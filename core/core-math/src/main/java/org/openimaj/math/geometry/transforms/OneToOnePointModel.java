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

import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.model.Model;
import org.openimaj.math.util.distance.DistanceCheck;
import org.openimaj.math.util.distance.ThresholdDistanceCheck;
import org.openimaj.util.pair.IndependentPair;

/**
 * A OneToOnePointModel models a one-to-one mapping of points
 * in a 2d space. For purposes of validation and error calculation,
 * a {@link DistanceCheck} object must be provided to test the
 * distance between a pair of points for point-equality.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class OneToOnePointModel implements Model<Point2d, Point2d> {
	protected DistanceCheck check;
	
	/**
	 * Convenience constructor that makes the underlying 
	 * {@link DistanceCheck} a {@link ThresholdDistanceCheck} with the
	 * given threshold. 
	 * @param threshold the threshold
	 */
	public OneToOnePointModel(float threshold) {
		this.check = new ThresholdDistanceCheck(threshold);
	}
	
	/**
	 * Construct with given DistanceCheck
	 * @param check the DistanceCheck
	 */
	public OneToOnePointModel(DistanceCheck check) {
		this.check = check;
	}
	
	@Override
	public void estimate(List<? extends IndependentPair<Point2d, Point2d>> data) {
		//OneToOnePointModel doesn't need estimating
	}

	@Override
	public boolean validate(IndependentPair<Point2d, Point2d> data) {
		double distance = Line2d.distance(data.firstObject(), data.secondObject());
		return check.check(distance);
	}

	@Override
	public Point2d predict(Point2d data) {
		return data;
	}

	@Override
	public int numItemsToEstimate() {
		return 0;
	}

	@Override
	public double calculateError(List<? extends IndependentPair<Point2d, Point2d>> data) {
		double error = 0;
		
		for (IndependentPair<Point2d, Point2d> d : data) {
			if (!validate(d)) error++;
		}
		
		return error / data.size();
	}
	
	@Override
	public OneToOnePointModel clone() {
		try {
			return (OneToOnePointModel) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
}
