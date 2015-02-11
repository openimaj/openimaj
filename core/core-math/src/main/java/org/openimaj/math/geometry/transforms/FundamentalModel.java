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
package org.openimaj.math.geometry.transforms;

import java.util.List;

import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.model.EstimatableModel;
import org.openimaj.math.model.fit.residuals.AbstractResidualCalculator;
import org.openimaj.math.model.fit.residuals.ResidualCalculator;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.util.pair.Pair;

import Jama.Matrix;

/**
 * Implementation of a Fundamental matrix model that estimates the epipolar
 * geometry.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class FundamentalModel implements EstimatableModel<Point2d, Point2d> {
	/**
	 * Computes the algebraic residual of the point pairs applied to the F
	 * matrix
	 *
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 *
	 */
	public static class Fundamental8PtResidual extends AbstractResidualCalculator<Point2d, Point2d, FundamentalModel> {
		@Override
		public double computeResidual(IndependentPair<Point2d, Point2d> data) {
			final Matrix F = model.fundamental;

			final Point2d p1 = data.firstObject();
			final Point2d p2 = data.secondObject();

			final float x1_1 = p1.getX(); // u
			final float x1_2 = p1.getY(); // v
			final float x2_1 = p2.getX(); // u'
			final float x2_2 = p2.getY(); // v'

			double res = 0;
			res += F.get(0, 0) * x2_1 * x1_1; // u' * u
			res += F.get(0, 1) * x2_1 * x1_2; // u' * v
			res += F.get(0, 2) * x2_1; // u'
			res += F.get(1, 0) * x2_2 * x1_1; // v' * u
			res += F.get(1, 1) * x2_2 * x1_2; // v' * v
			res += F.get(1, 2) * x2_2; // v'
			res += F.get(2, 0) * x1_1; // u
			res += F.get(2, 1) * x1_2; // v
			res += F.get(2, 2); // 1

			return res * res;
		}
	}

	/**
	 * Geometric residual that sums the distance of points from the closest
	 * epipolar line.
	 *
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	public static class EpipolarResidual extends AbstractResidualCalculator<Point2d, Point2d, FundamentalModel> {
		@Override
		public double computeResidual(IndependentPair<Point2d, Point2d> data) {
			final Matrix p1Mat = new Matrix(3, 1);
			final Matrix p2Mat = new Matrix(3, 1);

			// x
			p1Mat.set(0, 0, data.firstObject().getX());
			p1Mat.set(1, 0, data.firstObject().getY());
			p1Mat.set(2, 0, 1);

			// x'
			p2Mat.set(0, 0, data.secondObject().getX());
			p2Mat.set(1, 0, data.secondObject().getY());
			p2Mat.set(2, 0, 1);

			final Matrix l1 = model.fundamental.times(p1Mat);
			final double n1 = Math.sqrt(l1.get(0, 0) * l1.get(0, 0) + l1.get(1, 0) * l1.get(1, 0));
			final double d1 = Math.abs((l1.get(0, 0) * p2Mat.get(0, 0) + l1.get(1, 0) * p2Mat.get(1, 0) + l1.get(2, 0)
					* p2Mat.get(2, 0))
					/ n1);

			final Matrix l2 = model.fundamental.transpose().times(p2Mat);
			final double n2 = Math.sqrt(l2.get(0, 0) * l2.get(0, 0) + l2.get(1, 0) * l2.get(1, 0));
			final double d2 = Math.abs((l2.get(0, 0) * p1Mat.get(0, 0) + l2.get(1, 0) * p1Mat.get(1, 0) + l2.get(2, 0)
					* p1Mat.get(2, 0))
					/ n2);

			return d1 + d2;
		}
	}

	/**
	 * {@link ResidualCalculator} based on Sampson's geometric error.
	 *
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 *
	 */
	public static class SampsonGeometricResidual extends AbstractResidualCalculator<Point2d, Point2d, FundamentalModel> {
		@Override
		public double computeResidual(IndependentPair<Point2d, Point2d> data) {
			final Matrix p1 = new Matrix(3, 1);
			final Matrix p2 = new Matrix(3, 1);

			// x
			p1.set(0, 0, data.firstObject().getX());
			p1.set(1, 0, data.firstObject().getY());
			p1.set(2, 0, 1);

			// x'
			p2.set(0, 0, data.secondObject().getX());
			p2.set(1, 0, data.secondObject().getY());
			p2.set(2, 0, 1);

			final double p2tFp1 = p2.transpose().times(model.fundamental).times(p1).get(0, 0);
			final Matrix Fp1 = model.fundamental.times(p1);
			final Matrix Ftp2 = model.fundamental.transpose().times(p2);

			final double dist = (p2tFp1 * p2tFp1)
					/ (Fp1.get(0, 0) * Fp1.get(0, 0) + Fp1.get(1, 0) * Fp1.get(1, 0) + Ftp2.get(0, 0) * Ftp2.get(0, 0) + Ftp2
							.get(1, 0)
							* Ftp2.get(1, 0));

			return Math.abs(dist);
		}
	}

	protected boolean normalise;
	protected Matrix fundamental;

	/**
	 * Create a {@link FundamentalModel}, automatically normalising data when
	 * estimating the model
	 */
	public FundamentalModel()
	{
		this(true);
	}

	/**
	 * Create a {@link FundamentalModel} with optional automatic normalisation.
	 *
	 * @param norm
	 *            true if the data should be automatically normalised before
	 *            running the 8-point algorithm
	 */
	public FundamentalModel(boolean norm)
	{
		this.normalise = norm;
	}

	@Override
	public boolean estimate(List<? extends IndependentPair<Point2d, Point2d>> data) {
		if (normalise) {
			final Pair<Matrix> norms = TransformUtilities.getNormalisations(data);
			final List<? extends IndependentPair<Point2d, Point2d>> normData = TransformUtilities.normalise(data, norms);

			final Matrix normFundamental = TransformUtilities.fundamentalMatrix8Pt(normData);
			this.fundamental = norms.secondObject().transpose().times(normFundamental).times(norms.firstObject());
		} else {
			this.fundamental = TransformUtilities.fundamentalMatrix8Pt(data);
		}
		return true;
	}

	/**
	 * De-normalise a fundamental estimate. Use when {@link #estimate(List)} was
	 * called with pre-normalised data.
	 *
	 * @param norms
	 *            the normalisation transforms
	 */
	public void denormaliseFundamental(Pair<Matrix> norms) {
		this.fundamental = norms.secondObject().transpose().times(fundamental).times(norms.firstObject());
	}

	@Override
	public Point2d predict(Point2d data) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int numItemsToEstimate() {
		return 8;
	}

	/**
	 * Clone the model
	 *
	 * @return a cloned copy
	 */
	@Override
	public FundamentalModel clone() {
		final FundamentalModel model = new FundamentalModel(this.normalise);
		if (model.fundamental != null)
			model.fundamental = fundamental.copy();
		return model;
	}

	/**
	 * Set the Fundamental matrix
	 *
	 * @param optimised
	 */
	public void setF(Matrix optimised) {
		this.fundamental = optimised;
	}

	/**
	 * Get the fundamental matrix
	 *
	 * @return the F matrix
	 */
	public Matrix getF() {
		return fundamental;
	}
}
