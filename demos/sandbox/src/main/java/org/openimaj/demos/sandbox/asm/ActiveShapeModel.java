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

import org.openimaj.demos.sandbox.asm.landmark.LandmarkModel;
import org.openimaj.demos.sandbox.asm.landmark.LandmarkModelFactory;
import org.openimaj.image.FImage;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.shape.PointDistributionModel;
import org.openimaj.math.geometry.shape.PointDistributionModel.Constraint;
import org.openimaj.math.geometry.shape.PointList;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.util.pair.ObjectFloatPair;

import Jama.Matrix;

public class ActiveShapeModel {
	private PointDistributionModel pdm;
	private LandmarkModel<FImage>[] landmarkModels;
	private int maxIter = 25;
	
	public ActiveShapeModel(PointDistributionModel pdm, LandmarkModel<FImage>[] landmarkModels) {
		this.pdm = pdm;
		this.landmarkModels = landmarkModels;
	}
	
	public static ActiveShapeModel trainModel(int numComponents, List<IndependentPair<PointList, FImage>> data, Constraint constraint, LandmarkModelFactory<FImage> factory) {
		int nPoints = data.get(0).firstObject().size();
		
		@SuppressWarnings("unchecked")
		LandmarkModel<FImage>[] ppms = new LandmarkModel[nPoints];
		
		for (int i=0; i<data.size(); i++) {
			for (int j=0; j<nPoints; j++) {
				if (ppms[j] == null) {
					ppms[j] = factory.createLandmarkModel();
				}
			
				PointList pl = data.get(i).firstObject();
				
				ppms[j].updateModel(data.get(i).secondObject(), pl.get(j), pl);
			}
		}
		
		List<PointList> pls = new ArrayList<PointList>();
		for (IndependentPair<PointList, FImage> i : data)
			pls.add(i.firstObject());
		
		PointDistributionModel pdm = new PointDistributionModel(constraint, pls);
		pdm.setNumComponents(numComponents);
		
		return new ActiveShapeModel(pdm, ppms);
	}
	
	public static class IterationResult {
		public double fit;
		public PointList shape;
		public Matrix pose;
		public double [] parameters;

		public IterationResult(Matrix pose, PointList shape, double fit, double [] parameters) {
			this.pose = pose;
			this.shape = shape;
			this.fit = fit;
			this.parameters = parameters;
		}
	}
	
	public IterationResult performIteration(FImage image, PointList currentShape) {
		PointList newShape = new PointList();
		
		int inliers = 0;
		int outliers = 0;
		//compute updated points and a score based on how far they moved
		for (int i=0; i<landmarkModels.length; i++) {
			ObjectFloatPair<Point2d> newBest = landmarkModels[i].updatePosition(image, currentShape.get(i), currentShape);
			newShape.points.add( newBest.first );
			
			float percentageFromStart = newBest.second;
			if (percentageFromStart < 0.5)
				inliers++;
			else
				outliers++;
		}
		double score = ((double)inliers) / ((double)(inliers + outliers));
		
		//find the parameters and pose that "best" model the updated points
		IndependentPair<Matrix, double[]> newModelParams = pdm.fitModel(newShape);
		
		Matrix pose = newModelParams.firstObject();
		double[] parameters = newModelParams.secondObject();
		
		//apply model parameters to get final shape for the iteration
		newShape = pdm.generateNewShape(parameters).transform(pose);
		
		return new IterationResult(pose, newShape, score, parameters);
	}
	
	public IterationResult fit(FImage image, PointList initialShape) {
		IterationResult ir = performIteration(image, initialShape);
		int count = 0;
		
		while (ir.fit < 0.9 && count < maxIter) {
			ir = performIteration(image, ir.shape);
			count++;
		}
		
		return ir;
	}
	
	/**
	 * @return the {@link PointDistributionModel}
	 */
	public PointDistributionModel getPDM() {
		return pdm;
	}

	/**
	 * @return the local landmark appearance models; one for each point in the shape.
	 */
	public LandmarkModel<FImage>[] getLandmarkModels() {
		return landmarkModels;
	}
}
