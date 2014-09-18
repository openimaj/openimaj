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
 * The points in the first image are projected by the homography matrix to
 * produce new estimates of the second image points and the second image point
 * projected by the inverse homography to produce estimates of the first.
 * Residuals are computed from both point sets and summed to produce the final
 * geometric residual value.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <M>
 */
public class SymmetricTransferResidual2d<M extends Model<Point2d, Point2d> & MatrixTransformProvider>
		implements
		ResidualCalculator<Point2d, Point2d, M>
{
	private Matrix transform;
	private Matrix transformInv;

	@Override
	public void setModel(M model) {
		this.transform = model.getTransform();

		if (transform.getRowDimension() != 3 || transform.getColumnDimension() != 3)
			throw new IllegalArgumentException("Transform matrix must be 3x3");

		transformInv = transform.inverse();
	}

	@Override
	public double computeResidual(IndependentPair<Point2d, Point2d> data) {
		final Point2d p1 = data.getFirstObject();
		final Point2d p2 = data.getSecondObject();

		final Point2d p1t = p1.transform(transform);
		final Point2d p2t = p2.transform(transformInv);

		final float p1x = p1.getX();
		final float p1y = p1.getY();
		final float p1tx = p1t.getX();
		final float p1ty = p1t.getY();
		final float p2x = p2.getX();
		final float p2y = p2.getY();
		final float p2tx = p2t.getX();
		final float p2ty = p2t.getY();

		final float dx12t = (p1x - p2tx);
		final float dy12t = (p1y - p2ty);
		final float dx1t2 = (p1tx - p2x);
		final float dy1t2 = (p1ty - p2y);

		return dx12t * dx12t + dy12t * dy12t + dx1t2 * dx1t2 + dy1t2 * dy1t2;
	}

	@Override
	public void computeResiduals(List<? extends IndependentPair<Point2d, Point2d>> data, double[] residuals) {
		for (int i = 0; i < data.size(); i++) {
			residuals[i] = computeResidual(data.get(i));
		}
	}
}
