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
import org.openimaj.util.function.Predicate;
import org.openimaj.util.pair.IndependentPair;

import Jama.Matrix;

/**
 * Concrete implementation of a model of an Affine transform. Capable of
 * least-squares estimate of model parameters using the SVD.
 * 
 * @author Jonathon Hare
 * 
 */
public class AffineTransformModel implements EstimatableModel<Point2d, Point2d>, MatrixTransformProvider {
	protected Predicate<AffineTransformModel> modelCheck;
	protected Matrix transform;

	/**
	 * Create an {@link AffineTransformModel}
	 */
	public AffineTransformModel()
	{
		transform = new Matrix(3, 3);

		transform.set(2, 0, 0);
		transform.set(2, 1, 0);
		transform.set(2, 2, 1);
	}

	/**
	 * Create an {@link AffineTransformModel}. The given {@link Predicate} is
	 * used by the {@link #estimate(List)} method to test whether the estimated
	 * affine transform is sensible.
	 * 
	 * @param mc
	 *            the test function for sensible affine transforms
	 */
	public AffineTransformModel(Predicate<AffineTransformModel> mc)
	{
		this.modelCheck = mc;
		transform = new Matrix(3, 3);

		transform.set(2, 0, 0);
		transform.set(2, 1, 0);
		transform.set(2, 2, 1);
	}

	@Override
	public AffineTransformModel clone() {
		final AffineTransformModel atm = new AffineTransformModel();
		atm.modelCheck = modelCheck;
		atm.transform = transform.copy();
		return atm;
	}

	@Override
	public Matrix getTransform() {
		return transform;
	}

	/*
	 * SVD least-squares estimation of affine transform matrix for a set of 2d
	 * points
	 */
	@Override
	public boolean estimate(List<? extends IndependentPair<Point2d, Point2d>> data) {
		this.transform = TransformUtilities.affineMatrix(data);

		if (modelCheck == null)
			return true;

		return modelCheck.test(this);
	}

	@Override
	public Point2d predict(Point2d p) {
		return p.transform(transform);
	}

	@Override
	public int numItemsToEstimate() {
		return 3;
	}

	@Override
	public String toString() {
		String str = "";
		final double[][] mat = transform.getArray();
		for (final double[] r : mat) {
			for (final double v : r) {
				str += " " + v;
			}
			str += "\n";
		}
		return str;
	}
}
