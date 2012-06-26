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

import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.util.distance.DistanceCheck;
import org.openimaj.util.pair.IndependentPair;

import Jama.Matrix;

/**
 * A TransformedOneToOnePointModel is an extension of a OneToOnePointModel that allows
 * arbitary transform matrices to be applied to both point sets before equality testing.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class TransformedOneToOnePointModel extends OneToOnePointModel {
	protected Matrix secondTransform;
	protected Matrix firstTransform;
	
	/**
	 * Construct with the given transform matrices for transforming the
	 * points before comparison. The threshold parameter controls how far
	 * transformed points are allowed to be from each other to still be 
	 * considered the same. 
	 * 
	 * @param threshold the threshold.
	 * @param firstTransform the first transform matrix 
	 * @param secondTransform the second transform matrix
	 */
	public TransformedOneToOnePointModel(float threshold, Matrix firstTransform, Matrix secondTransform) {
		super(threshold);
		this.firstTransform = firstTransform;
		this.secondTransform = secondTransform;
	}
	
	/**
	 * Construct with the given transform matrices for transforming the
	 * points before comparison. The check parameter controls the method 
	 * for determining whether points are said to be equal based on their 
	 * distance apart.
	 * 
	 * @param check the DistanceCheck.
	 * @param firstTransform the first transform matrix 
	 * @param secondTransform the second transform matrix
	 */
	public TransformedOneToOnePointModel(DistanceCheck check, Matrix firstTransform, Matrix secondTransform) {
		super(check);
		this.firstTransform = firstTransform;
		this.secondTransform = secondTransform;
	}
	
	@Override
	public boolean validate(IndependentPair<Point2d, Point2d> data) {
		double distance = Line2d.distance(data.firstObject().transform(firstTransform), data.secondObject().transform(secondTransform));
		return check.check(distance);
	}
	
	@Override
	public TransformedOneToOnePointModel clone() {
		return (TransformedOneToOnePointModel) super.clone();
	}
}
