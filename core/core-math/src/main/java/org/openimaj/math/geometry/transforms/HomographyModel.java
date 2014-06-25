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
import org.openimaj.util.pair.IndependentPair;

import Jama.Matrix;

/**
 * Implementation of a Homogeneous Homography model - a transform that models
 * the relationship between planes under projective constraints (8 D.o.F)
 * 
 * @author Jonathon Hare
 * 
 */
public class HomographyModel implements EstimatableModel<Point2d, Point2d>, MatrixTransformProvider {
	protected Matrix homography;

	/**
	 * Create an {@link HomographyModel}
	 */
	public HomographyModel()
	{
		homography = new Matrix(3, 3);
	}

	@Override
	public HomographyModel clone() {
		final HomographyModel hm = new HomographyModel();
		hm.homography = homography.copy();
		return hm;
	}

	@Override
	public Matrix getTransform() {
		return homography;
	}

	/**
	 * DLT estimation of least-squares solution of 3D homogeneous homography
	 * 
	 * @see org.openimaj.math.model.EstimatableModel#estimate(java.util.List)
	 */
	@Override
	public void estimate(List<? extends IndependentPair<Point2d, Point2d>> data) {
		homography = TransformUtilities.homographyMatrix(data);
	}

	@Override
	public int numItemsToEstimate() {
		return 4;
	}

	@Override
	public Point2d predict(Point2d data) {
		return data.transform(homography);
	}
}
