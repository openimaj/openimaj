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
package org.openimaj.image.model.asm;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.citation.annotation.References;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.analysis.pyramid.SimplePyramid;
import org.openimaj.image.model.asm.ActiveShapeModel.IterationResult;
import org.openimaj.image.model.landmark.LandmarkModel;
import org.openimaj.image.model.landmark.LandmarkModelFactory;
import org.openimaj.image.processor.SinglebandImageProcessor;
import org.openimaj.math.geometry.point.PointList;
import org.openimaj.math.geometry.shape.PointDistributionModel;
import org.openimaj.math.geometry.shape.PointDistributionModel.Constraint;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.math.matrix.algorithm.pca.PrincipalComponentAnalysis.ComponentSelector;
import org.openimaj.util.pair.IndependentPair;

import Jama.Matrix;

/**
 * Implementation of a basic Multi-resolution Active Shape Model. 
 * The implementation allows different types of landmark appearance 
 * models and can work with both colour and greylevel images.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <I> Concrete type of {@link Image}
 */
@References(references = { 
		@Reference(
				author = { "Cootes, T. F.", "Taylor, C. J." }, 
				title = "Statistical Models of Appearance for Computer Vision", 
				type = ReferenceType.Unpublished,
				month = "October",
				year = "2001",
				url = "http://isbe.man.ac.uk/~bim/Models/app_model.ps.gz"
		),
		@Reference(
				type = ReferenceType.Inproceedings,
				author = { "Cootes, T F", "Taylor, C J", "Lanitis, A" },
				title = "Active shape models: Evaluation of a multi-resolution method for improving image search",
				year = "1994",
				booktitle = "Proc British Machine Vision Conference",
				pages = { "327", "", "336" },
				url = "http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.141.4937&rep=rep1&type=pdf",
				editor = { "Hancock, E" },
				publisher = "BMVA Press",
				volume = "1"
		)
})
public class MultiResolutionActiveShapeModel<I extends Image<?, I> & SinglebandImageProcessor.Processable<Float,FImage,I>> {
	private int numLevels; //num resolutions
	private ActiveShapeModel<I>[] asms;
	private static float sigma = 0.5f;

	/**
	 * Construct a {@link MultiResolutionActiveShapeModel} from the
	 * stack of provided {@link ActiveShapeModel}s. The ASMs should
	 * be arranged in order of decreasing resolution.
	 * 
	 * @param asms
	 */
	public MultiResolutionActiveShapeModel(ActiveShapeModel<I>[] asms) {
		this.numLevels = asms.length;
		this.asms = asms;
	}

	/**
	 * Train a new {@link MultiResolutionActiveShapeModel} from the given
	 * data.
	 * 
	 * @param <I> Concrete type of {@link Image}
	 * @param numLevels number of levels in the pyramid (scales)
	 * @param selector a {@link ComponentSelector} for choosing significant EVs
	 * @param data annotated images for training
	 * @param constraint a {@link Constraint} for constraining plausible shapes from the {@link PointDistributionModel}.
	 * @param factory a {@link LandmarkModelFactory} for producing models of local appearance around the landmarks.
	 * @return a newly trained {@link MultiResolutionActiveShapeModel}.
	 */
	public static <I extends Image<?, I> & SinglebandImageProcessor.Processable<Float,FImage,I>> MultiResolutionActiveShapeModel<I> 
	trainModel(int numLevels, ComponentSelector selector, List<IndependentPair<PointList, I>> data, Constraint constraint, LandmarkModelFactory<I> factory) {
		int nPoints = data.get(0).firstObject().size();

		@SuppressWarnings("unchecked")
		LandmarkModel<I>[][] ppms = new LandmarkModel[numLevels][nPoints];

		for (int i=0; i<data.size(); i++) {
			SimplePyramid<I> pyr = SimplePyramid.createGaussianPyramid(data.get(i).secondObject(), sigma, numLevels);
			PointList pl = data.get(i).firstObject();

			for (int level=0; level<numLevels; level++) {
				Matrix scaling = TransformUtilities.scaleMatrix(1.0/Math.pow(2, level), 1.0/Math.pow(2, level));
				PointList tfpl = pl.transform(scaling);
				I image = pyr.pyramid[level];

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
		for (IndependentPair<PointList, I> i : data)
			pls.add(i.firstObject());

		PointDistributionModel pdm = new PointDistributionModel(constraint, pls);
		pdm.setNumComponents(selector);

		@SuppressWarnings("unchecked")
		ActiveShapeModel<I> [] asms = new ActiveShapeModel[numLevels]; 
		for (int level=0; level<numLevels; level++) {
			asms[level] = new ActiveShapeModel<I>(pdm, ppms[level]);
		}

		return new MultiResolutionActiveShapeModel<I>(asms);
	}

	/**
	 * Perform multi-resolution fitting of the initial shape to
	 * the initial image.
	 * 
	 * @param initialImage the initial shape.
	 * @param initialShape the initial image.
	 * @return the fitted model parameters.
	 */
	public IterationResult fit(I initialImage, PointList initialShape) {
		SimplePyramid<I> pyr = SimplePyramid.createGaussianPyramid(initialImage, sigma, numLevels);

		Matrix scaling = TransformUtilities.scaleMatrix(1.0/Math.pow(2, numLevels-1), 1.0/Math.pow(2, numLevels-1));

		PointList shape = initialShape.transform(scaling);
		Matrix pose = null;
		double [] parameters = null;

		double fit = 0;
		for (int level=numLevels-1; level>=0; level--) {
			I image = pyr.pyramid[level];

			ActiveShapeModel<I> asm = asms[level];

			IterationResult newData = asm.fit(image, shape);

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
