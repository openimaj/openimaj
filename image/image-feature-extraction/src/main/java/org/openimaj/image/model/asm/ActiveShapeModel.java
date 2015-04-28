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
import org.openimaj.image.Image;
import org.openimaj.image.model.landmark.LandmarkModel;
import org.openimaj.image.model.landmark.LandmarkModelFactory;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.PointList;
import org.openimaj.math.geometry.shape.PointDistributionModel;
import org.openimaj.math.geometry.shape.PointDistributionModel.Constraint;
import org.openimaj.math.matrix.algorithm.pca.PrincipalComponentAnalysis.ComponentSelector;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.util.pair.ObjectFloatPair;

import Jama.Matrix;

/**
 * Implementation of a basic Active Shape Model. The implementation allows
 * different types of landmark appearance models and can work with both colour
 * and greylevel images.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <I>
 *            Concrete type of {@link Image}
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
						author = { "T. F. Cootes", "C. J. Taylor" },
						title = "Active Shape Models",
						year = "1992",
						booktitle = "in Proceedings of the British Machine Vision Conference"
						)
})
public class ActiveShapeModel<I extends Image<?, I>> {
	private PointDistributionModel pdm;
	private LandmarkModel<I>[] landmarkModels;
	private int maxIter = 50;
	private double inlierPercentage = 0.9;

	/**
	 * Construct an {@link ActiveShapeModel} from a pre-trained
	 * {@link PointDistributionModel} and set of {@link LandmarkModel}s.
	 * 
	 * @param pdm
	 *            the {@link PointDistributionModel}.
	 * @param landmarkModels
	 *            the {@link LandmarkModel}s.
	 */
	public ActiveShapeModel(PointDistributionModel pdm, LandmarkModel<I>[] landmarkModels) {
		this.pdm = pdm;
		this.landmarkModels = landmarkModels;
	}

	/**
	 * Train a new {@link ActiveShapeModel} using the given data and parameters.
	 *
	 * @param <I>
	 *            The concrete image type.
	 * @param selector
	 *            the selector for choosing the number of principal components /
	 *            modes of the model.
	 * @param data
	 *            the data to train the model from
	 * @param constraint
	 *            the constraint to apply to restrict the model to plausible
	 *            shapes.
	 * @param factory
	 *            the {@link LandmarkModelFactory} for learning local appearance
	 *            models
	 * @return a newly trained {@link ActiveShapeModel}.
	 */
	public static <I extends Image<?, I>> ActiveShapeModel<I> trainModel(ComponentSelector selector,
			List<IndependentPair<PointList, I>> data, Constraint constraint, LandmarkModelFactory<I> factory)
	{
		final int nPoints = data.get(0).firstObject().size();

		@SuppressWarnings("unchecked")
		final LandmarkModel<I>[] ppms = new LandmarkModel[nPoints];

		for (int i = 0; i < data.size(); i++) {
			for (int j = 0; j < nPoints; j++) {
				if (ppms[j] == null) {
					ppms[j] = factory.createLandmarkModel();
				}

				final PointList pl = data.get(i).firstObject();

				ppms[j].updateModel(data.get(i).secondObject(), pl.get(j), pl);
			}
		}

		final List<PointList> pls = new ArrayList<PointList>();
		for (final IndependentPair<PointList, I> i : data)
			pls.add(i.firstObject());

		final PointDistributionModel pdm = new PointDistributionModel(constraint, pls);
		pdm.setNumComponents(selector);

		return new ActiveShapeModel<I>(pdm, ppms);
	}

	/**
	 * Class to hold the response of a single iteration of model fitting.
	 *
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 *
	 */
	public static class IterationResult {
		/**
		 * The percentage of points that moved less than 50% of their allowed
		 * distance
		 */
		public double fit;
		/**
		 * The updated shape in image coordinates
		 */
		public PointList shape;
		/**
		 * The model pose from model coordinates to image coordinates
		 */
		public Matrix pose;
		/**
		 * The model weight parameters
		 */
		public double[] parameters;

		protected IterationResult(Matrix pose, PointList shape, double fit, double[] parameters) {
			this.pose = pose;
			this.shape = shape;
			this.fit = fit;
			this.parameters = parameters;
		}
	}

	/**
	 * Perform a single iteration of model fitting.
	 *
	 * @param image
	 *            the image to fit to
	 * @param currentShape
	 *            the starting shape in image coordinates
	 * @return the updated shape and parameters
	 */
	public IterationResult performIteration(I image, PointList currentShape) {
		PointList newShape = new PointList();

		int inliers = 0;
		int outliers = 0;
		// compute updated points and a score based on how far they moved
		for (int i = 0; i < landmarkModels.length; i++) {
			final ObjectFloatPair<Point2d> newBest = landmarkModels[i].updatePosition(image, currentShape.get(i),
					currentShape);
			newShape.points.add(newBest.first);

			final float percentageFromStart = newBest.second;
			if (percentageFromStart < 0.5)
				inliers++;
			else
				outliers++;
		}
		final double score = ((double) inliers) / ((double) (inliers + outliers));

		// find the parameters and pose that "best" model the updated points
		final IndependentPair<Matrix, double[]> newModelParams = pdm.fitModel(newShape);

		final Matrix pose = newModelParams.firstObject();
		final double[] parameters = newModelParams.secondObject();

		// apply model parameters to get final shape for the iteration
		newShape = pdm.generateNewShape(parameters).transform(pose);

		return new IterationResult(pose, newShape, score, parameters);
	}

	/**
	 * Iteratively apply {@link #performIteration(Image, PointList)} until the
	 * maximum number of iterations is exceeded, or the number of points that
	 * moved less than 0.5 of their maximum distance in an iteration is less
	 * than the target inlier percentage.
	 *
	 * @see #setInlierPercentage(double)
	 * @see #setMaxIterations(int)
	 *
	 * @param image
	 *            the image to fit the shape to
	 * @param initialShape
	 *            the initial shape in image coordinates
	 * @return the fitted shape and parameters
	 */
	public IterationResult fit(I image, PointList initialShape) {
		IterationResult ir = performIteration(image, initialShape);
		int count = 0;

		while (ir.fit < inlierPercentage && count < maxIter) {
			ir = performIteration(image, ir.shape);
			count++;
		}

		return ir;
	}

	/**
	 * @return the maxIter
	 */
	public int getMaxIterations() {
		return maxIter;
	}

	/**
	 * Set the maximum allowed number of iterations in fitting the model
	 * 
	 * @param maxIter
	 *            the maxIter to set
	 */
	public void setMaxIterations(int maxIter) {
		this.maxIter = maxIter;
	}

	/**
	 * @return the inlierPercentage
	 */
	public double getInlierPercentage() {
		return inlierPercentage;
	}

	/**
	 * Set the target percentage of the number of points that move less than 0.5
	 * of their total possible distance within an iteration to stop fitting.
	 * 
	 * @param inlierPercentage
	 *            the inlierPercentage to set
	 */
	public void setInlierPercentage(double inlierPercentage) {
		this.inlierPercentage = inlierPercentage;
	}

	/**
	 * @return the learnt {@link PointDistributionModel}
	 */
	public PointDistributionModel getPDM() {
		return pdm;
	}

	/**
	 * @return the local landmark appearance models; one for each point in the
	 *         shape.
	 */
	public LandmarkModel<I>[] getLandmarkModels() {
		return landmarkModels;
	}
}
