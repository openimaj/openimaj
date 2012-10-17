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
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * Abstract base class for implementations of {@link MultiScaleObjectDetector}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <IMAGE>
 *            The image on which to perform the detection
 * @param <DETECTED_OBJECT>
 *            the type of object representing a detection.
 */
public abstract class AbstractMultiScaleObjectDetector<IMAGE extends Image<?, IMAGE>, DETECTED_OBJECT>
		implements
			MultiScaleObjectDetector<IMAGE, DETECTED_OBJECT>
{
	protected Rectangle roi;
	protected int minSize = 0;
	protected int maxSize = 0;

	/**
	 * Construct with the initial minimum and maximum size set to zero.
	 */
	protected AbstractMultiScaleObjectDetector() {
	}

	/**
	 * Construct with the given initial minimum and maximum detection sizes.
	 * 
	 * @param minSize
	 *            minimum size
	 * @param maxSize
	 *            maximum size
	 */
	protected AbstractMultiScaleObjectDetector(int minSize, int maxSize) {
		this.minSize = minSize;
		this.maxSize = maxSize;
	}

	@Override
	public void setROI(Rectangle roi) {
		this.roi = roi;
	}

	@Override
	public void setMinimumDetectionSize(int size) {
		this.minSize = size;
	}

	@Override
	public void setMaximumDetectionSize(int size) {
		this.maxSize = size;
	}

	@Override
	public int getMinimumDetectionSize() {
		return minSize;
	}

	@Override
	public int getMaximumDetectionSize() {
		return maxSize;
	}
}
