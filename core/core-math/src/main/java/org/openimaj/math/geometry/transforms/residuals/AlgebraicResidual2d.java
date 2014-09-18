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
import org.openimaj.math.geometry.transforms.MatrixTransformProvider;
import org.openimaj.math.model.Model;
import org.openimaj.math.model.fit.residuals.ResidualCalculator;
import org.openimaj.util.pair.IndependentPair;

import Jama.Matrix;

/**
 * Compute the algebraic residuals of points mapped by a 2d homogeneous
 * transform (i.e. a 3x3 transform matrix). This is equivalent to the residuals
 * minimised when using the Direct Linear Transform method (i.e. minimising
 * |Ah|) used for estimating the transform.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <M>
 */
public class AlgebraicResidual2d<M extends Model<Point2d, Point2d> & MatrixTransformProvider>
		implements
		ResidualCalculator<Point2d, Point2d, M>
{
	private Matrix transform;

	@Override
	public void setModel(M model) {
		this.transform = model.getTransform();

		if (transform.getRowDimension() != 3 || transform.getColumnDimension() != 3)
			throw new IllegalArgumentException("Transform matrix must be 3x3");
	}

	@Override
	public double computeResidual(IndependentPair<Point2d, Point2d> data) {
		final Point2d p1 = data.getFirstObject();
		final Point2d p2 = data.getSecondObject();

		final float x = p1.getX();
		final float y = p1.getY();
		final float X = p2.getX();
		final float Y = p2.getY();

		final double h11 = transform.get(0, 0);
		final double h12 = transform.get(0, 1);
		final double h13 = transform.get(0, 2);
		final double h21 = transform.get(1, 0);
		final double h22 = transform.get(1, 1);
		final double h23 = transform.get(1, 2);
		final double h31 = transform.get(2, 0);
		final double h32 = transform.get(2, 1);
		final double h33 = transform.get(2, 2);

		final double s1 = x * h11 + y * h12 + h13 - x * X * h31 - y * X * h32 - X * h33;
		final double s2 = x * h21 + y * h22 + h23 - x * Y * h31 - y * Y * h32 - Y * h33;

		return s1 * s1 + s2 * s2;
	}

	@Override
	public void computeResiduals(List<? extends IndependentPair<Point2d, Point2d>> data, double[] residuals) {
		for (int i = 0; i < data.size(); i++) {
			residuals[i] = computeResidual(data.get(i));
		}
	}
}
