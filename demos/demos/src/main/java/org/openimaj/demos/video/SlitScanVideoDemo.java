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
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.capture.VideoCaptureException;
import org.openimaj.video.processing.effects.SlitScanProcessor;

/**
 * Demo showing a slit-scan effect being applied in real-time.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
@Demo(
		author = "Sina Samangooei",
		description = "Realtime demonstration of the slit-scan " +
				"video effect. The video is rendered as temporal scan-lines, " +
				"with the newest frames appearing at the top and " +
				"oldest at the bottom.",
		keywords = { "video", "effects", "slit-scan" },
		title = "Slit-Scan Effect",
		vmArguments = "-Xmx2G")
public class SlitScanVideoDemo {
	/**
	 * The main method
	 * 
	 * @param args
	 *            ignored
	 * @throws VideoCaptureException
	 */
	public static void main(String[] args) throws VideoCaptureException {
		final VideoCapture capture = new VideoCapture(640, 480);

		VideoDisplay.createVideoDisplay(new SlitScanProcessor(capture, 240));
	}
}
