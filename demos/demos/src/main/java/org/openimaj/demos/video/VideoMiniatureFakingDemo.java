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
package org.openimaj.demos.video;

import org.openimaj.demos.Demo;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.effects.DioramaEffect;
import org.openimaj.image.processor.ProcessorUtilities;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.capture.VideoCaptureException;
import org.openimaj.video.processor.VideoFrameProcessor;

/**
 * Real-time miniature faking.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Demo(
		author = "Jonathon Hare",
		description = "Realtime demo of miniature faking (the 'Diorama Effect').",
		keywords = { "video", "effect", "diorama", "miniature faking" },
		title = "Miniature Faking")
public class VideoMiniatureFakingDemo {
	/**
	 * Main method
	 * 
	 * @param args
	 *            ignored
	 * @throws VideoCaptureException
	 */
	public static void main(String[] args) throws VideoCaptureException {
		final int w = 640;
		final int h = 480;
		final Line2d axis = new Line2d(w / 2, h / 2, w / 2, h);

		VideoDisplay.createVideoDisplay(new VideoFrameProcessor<MBFImage>(new VideoCapture(w, h),
				ProcessorUtilities.wrap(new DioramaEffect(axis))));
	}
}
