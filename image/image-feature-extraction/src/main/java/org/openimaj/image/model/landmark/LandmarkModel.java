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
package org.openimaj.image.model.landmark;

import org.openimaj.image.Image;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.PointList;
import org.openimaj.util.pair.ObjectFloatPair;

/**
 * A {@link LandmarkModel} models local image content and provides functionality
 * to move a point in an image to a nearby point with a lower cost than at the
 * initial point.
 * <p>
 * Landmarks are normally associated with points in {@link PointList}s and thus
 * quite often have an associated "intrinsic scale". This scale can be used to
 * dynamically change the size of the support region of a landmark so that is
 * scales with the {@link PointList}s intrinsic scale.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <I>
 *            Type of image
 */
public interface LandmarkModel<I extends Image<?, I>> {
	/**
	 * Update the internal model of local image content by adding information
	 * from the provided image.
	 * 
	 * @param image
	 *            the image
	 * @param point
	 *            the point in the image representing the landmark
	 * @param pointList
	 *            the pointList to which the point belongs
	 */
	public void updateModel(I image, Point2d point, PointList pointList);

	/**
	 * Evaluate the cost function using the given image and point. Lower costs
	 * indicate better fits.
	 * 
	 * @param image
	 *            the image
	 * @param point
	 *            the point in the image
	 * @param pointList
	 *            the pointList to which the point belongs
	 * @return the cost
	 */
	public float computeCost(I image, Point2d point, PointList pointList);

	/**
	 * Estimate an improved fit based on a local neighbourhood search. Returns
	 * the new best point and the distance moved normalised by the size of the
	 * search area. If the point didn't move, then the distance would be 0; if
	 * the point moved to the extremity of the search region, it would be 1.0.
	 * 
	 * @param image
	 *            the image
	 * @param initial
	 *            the initial point in the image
	 * @param pointList
	 *            the pointList to which the point belongs
	 * @return the updated point
	 */
	public ObjectFloatPair<Point2d> updatePosition(I image, Point2d initial, PointList pointList);
}
