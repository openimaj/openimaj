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
package org.openimaj.video;

import org.openimaj.image.Image;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.video.timecode.VideoTimecode;

/**
 * A {@link VideoFrame} with a subwindow defined
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 * @param <T>
 */
public class VideoSubFrame<T extends Image<?, T>> extends VideoFrame<T> {
	/**
	 * The region of interest defining the sub-window.
	 */
	public Rectangle roi;
	private VideoFrame<T> cachedSubFrame;

	/**
	 * Constructor
	 * 
	 * @param frame
	 *            The frame
	 * @param timecode
	 *            The timecode
	 * @param rectangle
	 *            The subwindow into the frame
	 */
	public VideoSubFrame(T frame, VideoTimecode timecode, Rectangle rectangle) {
		super(frame, timecode);
		this.roi = rectangle;
	}

	/**
	 * @return the subframe as a {@link VideoFrame}
	 */
	public VideoFrame<T> extract() {
		if (cachedSubFrame == null) {
			cachedSubFrame = new VideoFrame<T>(frame.extractROI(roi), timecode);
		}
		return cachedSubFrame;
	}
}
