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
package org.openimaj.examples.video.capture;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.SwingUtilities;

import org.openimaj.image.MBFImage;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.capture.VideoCaptureException;

/**
 * Example showing how to open the default video capture device and display it
 * in a window. Also shows how to hook up a key listener so that the capture
 * pauses/unpauses when the spacebar is pressed.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class VideoCaptureExample extends KeyAdapter {
	VideoDisplay<MBFImage> display;

	VideoCaptureExample() throws VideoCaptureException {
		// open the capture device and create a window to display in
		display = VideoDisplay.createVideoDisplay(new VideoCapture(320, 240));

		// set the key listener of the display to this
		SwingUtilities.getRoot(display.getScreen()).addKeyListener(this);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// space pauses the video
		if (e.getKeyChar() == ' ')
			display.togglePause();
	}

	/**
	 * Main method.
	 * 
	 * @param args
	 *            ignored
	 * @throws VideoCaptureException
	 *             if their is a problem with the video capture hardware
	 */
	public static void main(String[] args) throws VideoCaptureException {
		new VideoCaptureExample();
	}
}
