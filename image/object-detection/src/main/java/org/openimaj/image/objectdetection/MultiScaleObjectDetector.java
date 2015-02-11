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
package org.openimaj.image.objectdetection;

import org.openimaj.image.Image;
import org.openimaj.image.objectdetection.filtering.DetectionFilter;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * Interface describing a multi-scale object detector. Optional methods are
 * provided for controlling the detection size.
 * <p>
 * Any type of Java object can be used to represent a detection; this could be
 * something as simple as a {@link Rectangle} representing the spatial location
 * of the detection, or it could be much more complex.
 * <p>
 * The interface allows for multiple detections to be returned from the input
 * image. If required, these detections could be filtered by a
 * {@link DetectionFilter} as a post-processing operation.
 *
 * @see ObjectDetector
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <IMAGE>
 *            The image on which to perform the detection
 * @param <DETECTED_OBJECT>
 *            the type of object representing a detection.
 */
public interface MultiScaleObjectDetector<IMAGE extends Image<?, IMAGE>, DETECTED_OBJECT>
		extends
ObjectDetector<IMAGE, DETECTED_OBJECT>
{
	/**
	 * (Optional operation).
	 * <p>
	 * Set the minimum detection size.
	 *
	 * @param size
	 *            the minimum detection size.
	 */
	public void setMinimumDetectionSize(int size);

	/**
	 * (Optional operation).
	 * <p>
	 * Set the maximum detection size. A size less than or equal to 0 indicates
	 * there is no maximum size.
	 *
	 * @param size
	 *            the maximum detection size.
	 */
	public void setMaximumDetectionSize(int size);

	/**
	 * (Optional operation).
	 * <p>
	 * Get the minimum detection size.
	 *
	 * @return the minimum detection size.
	 *
	 */
	public int getMinimumDetectionSize();

	/**
	 * (Optional operation).
	 * <p>
	 * Get the maximum detection size. A size less than or equal to 0 indicates
	 * there is no maximum size.
	 *
	 * @return the maximum detection size.
	 */
	public int getMaximumDetectionSize();
}
