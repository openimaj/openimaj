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
package org.openimaj.math.geometry.transforms.residuals;

import java.util.List;

import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.model.Model;
import org.openimaj.math.model.fit.residuals.ResidualCalculator;
import org.openimaj.util.pair.IndependentPair;

import Jama.Matrix;

/**
 * An implementation of a {@link SingleImageTransferResidual2d} that
 * pre-transforms both sets of points by predetermined transforms.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <M>
 *            type of model
 * 
 */
public class TransformedSITR2d<M extends Model<Point2d, Point2d>> implements ResidualCalculator<Point2d, Point2d, M> {
	Matrix t1;
	Matrix t2;
	M model;

	/**
	 * Construct with the given transforms
	 * 
	 * @param t1
	 *            transform for first point in the pair
	 * @param t2
	 *            transform for second point in the pair
	 */
	public TransformedSITR2d(Matrix t1, Matrix t2) {
		this.t1 = t1;
		this.t2 = t2;
	}

	@Override
	public void setModel(M model) {
		this.model = model;
	}

	@Override
	public double computeResidual(IndependentPair<Point2d, Point2d> data) {
		final Point2d p2_est = model.predict(data.firstObject()).transform(t1);

		final Point2d so = data.secondObject().transform(t2);
		final float dx = so.getX() - p2_est.getX();
		final float dy = so.getY() - p2_est.getY();

		return (dx * dx + dy * dy);
	}

	@Override
	public void computeResiduals(List<? extends IndependentPair<Point2d, Point2d>> data, double[] errors) {
		for (int i = 0; i < data.size(); i++) {
			final Point2d p2_est = model.predict(data.get(i).firstObject()).transform(t1);

			final Point2d so = data.get(i).secondObject().transform(t2);
			final float dx = so.getX() - p2_est.getX();
			final float dy = so.getY() - p2_est.getY();

			errors[i] = (dx * dx + dy * dy);
		}
	}
}
