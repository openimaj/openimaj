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
package org.openimaj.examples.video;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.SwingUtilities;

import org.openimaj.image.MBFImage;
import org.openimaj.video.Video;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.xuggle.XuggleVideoWriter;

/**
 * Record the webcam to a file.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class VideoRecordingExample extends KeyAdapter implements VideoDisplayListener<MBFImage> {
	private Video<MBFImage> video;
	private VideoDisplay<MBFImage> display;
	private XuggleVideoWriter writer;
	private boolean close = false;

	/**
	 * Default constructor
	 * 
	 * @throws IOException
	 */
	public VideoRecordingExample() throws IOException {
		// open webcam
		video = new VideoCapture(320, 240);

		// open display
		display = VideoDisplay.createVideoDisplay(video);

		// open a writer
		writer = new XuggleVideoWriter("video.flv", video.getWidth(), video.getHeight(), 30);

		// set this class to listen to video display events
		display.addVideoListener(this);

		// set this class to listen to keyboard events
		SwingUtilities.getRoot(display.getScreen()).addKeyListener(this);
	}

	@Override
	public void afterUpdate(VideoDisplay<MBFImage> display) {
		// Do nothing
	}

	@Override
	public void beforeUpdate(MBFImage frame) {
		// write a frame
		if (!close) {
			writer.addFrame(frame);
		}
	}

	@Override
	public void keyPressed(KeyEvent key) {
		// wait for the escape key to be pressed
		close = key.getKeyCode() == KeyEvent.VK_ESCAPE;
	}

	/**
	 * Main method
	 * 
	 * @param args
	 *            ignored
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		new VideoRecordingExample();
	}
}
