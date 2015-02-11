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
package org.openimaj.examples.video.processing.motion;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.capture.VideoCaptureException;

/**
 * Simple illustration of how motion or scene changes can be detected by simple
 * frame differencing
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class SimpleFrameDifferenceMotionDetector {
	/**
	 * @param args
	 * @throws VideoCaptureException
	 */
	public static void main(String[] args) throws VideoCaptureException {
		// Setup live capture
		final VideoCapture c = new VideoCapture(320, 240);

		// get the first frame
		FImage last = c.getNextFrame().flatten();
		// iterate through the frames
		for (final MBFImage frame : c) {
			final FImage current = frame.flatten();

			// compute the squared difference from the last frame
			float val = 0;
			for (int y = 0; y < current.height; y++) {
				for (int x = 0; x < current.width; x++) {
					final float diff = (current.pixels[y][x] - last.pixels[y][x]);
					val += diff * diff;
				}
			}

			// might need adjust threshold:
			if (val > 10) {
				System.out.println("motion");
			}

			// set the current frame to the last frame
			last = current;
		}

		c.close();
	}
}
