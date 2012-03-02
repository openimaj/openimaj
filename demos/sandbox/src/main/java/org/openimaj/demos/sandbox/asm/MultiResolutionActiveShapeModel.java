/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.demos.sandbox.asm;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.demos.sandbox.asm.ActiveShapeModel.IterationResult;
import org.openimaj.demos.sandbox.asm.landmark.LandmarkModel;
import org.openimaj.demos.sandbox.asm.landmark.LandmarkModelFactory;
import org.openimaj.image.FImage;
import org.openimaj.image.analysis.pyramid.SimplePyramid;
import org.openimaj.math.geometry.shape.PointDistributionModel;
import org.openimaj.math.geometry.shape.PointDistributionModel.Constraint;
import org.openimaj.math.geometry.shape.PointList;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.util.pair.IndependentPair;

import Jama.Matrix;

public class MultiResolutionActiveShapeModel {
	int l; //num resolutions
	ActiveShapeModel [] asms;
	static float sigma = 0.5f;

	public MultiResolutionActiveShapeModel(int l, ActiveShapeModel[] asms) {
		this.l = l;
		this.asms = asms;
	}

	public static MultiResolutionActiveShapeModel trainModel(int l, int numComponents, List<IndependentPair<PointList, FImage>> data, Constraint constraint, LandmarkModelFactory<FImage> factory) {
		int nPoints = data.get(0).firstObject().size();

		@SuppressWarnings("unchecked")
		LandmarkModel<FImage>[][] ppms = new LandmarkModel[l][nPoints];
		
		for (int i=0; i<data.size(); i++) {
			SimplePyramid<FImage> pyr = SimplePyramid.createGaussianPyramid(data.get(i).secondObject(), sigma, l);
			PointList pl = data.get(i).firstObject();
			
			for (int level=0; level<l; level++) {
				Matrix scaling = TransformUtilities.scaleMatrix(1.0/Math.pow(2, level), 1.0/Math.pow(2, level));
				PointList tfpl = pl.transform(scaling);
				FImage image = pyr.pyramid[level];
				
				for (int j=0; j<nPoints; j++) {
					if (ppms[level][j] == null) {
						//scale so the effective search area gets bigger with levels
						//i.e. if the "size" of the search area is 0.1 in the 0th level,
						//it would be 0.1 * scaleFactor in the 1st level and thus cover
						//more of the image
						ppms[level][j] = factory.createLandmarkModel((float) Math.pow(2, level));
					}

					ppms[level][j].updateModel(image, tfpl.get(j), tfpl);
				}
			}
		}

		List<PointList> pls = new ArrayList<PointList>();
		for (IndependentPair<PointList, FImage> i : data)
			pls.add(i.firstObject());

		PointDistributionModel pdm = new PointDistributionModel(constraint, pls);
		pdm.setNumComponents(numComponents);
		
		ActiveShapeModel [] asms = new ActiveShapeModel[l]; 
		for (int level=0; level<l; level++) {
			asms[level] = new ActiveShapeModel(pdm, ppms[level]);
		}
		
		return new MultiResolutionActiveShapeModel(l, asms);
	}
	
	public IterationResult fit(FImage initialImage, PointList initialShape) {
		SimplePyramid<FImage> pyr = SimplePyramid.createGaussianPyramid(initialImage, sigma, l);
		
		Matrix scaling = TransformUtilities.scaleMatrix(1.0/Math.pow(2, l-1), 1.0/Math.pow(2, l-1));
		
		PointList shape = initialShape.transform(scaling);
		Matrix pose = null;
		double [] parameters = null;
		
//		DisplayUtilities.displayName(asms[l-1].drawShapeAndNormals(pyr.pyramid[l-1], shape), "shape", true);
		
		double fit = 0;
		for (int level=l-1; level>=0; level--) {
			FImage image = pyr.pyramid[level];
			
			ActiveShapeModel asm = asms[level];
			
			IterationResult newData = asm.fit(image, shape);
			
//			DisplayUtilities.displayName(asm.drawShapeAndNormals(image, newData.shape), "shape", true);
			
			if (level == 0)
				scaling = Matrix.identity(3, 3);
			else
				scaling = TransformUtilities.scaleMatrix(2, 2);
			
			shape = newData.shape.transform(scaling);
			pose = newData.pose.times(scaling);
			fit  = newData.fit;
			parameters = newData.parameters;
		}
		
		return new IterationResult(pose, shape, fit, parameters);
	}

	/**
	 * @return the {@link PointDistributionModel}
	 */
	public PointDistributionModel getPDM() {
		return asms[0].getPDM();
	}
}
