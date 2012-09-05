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
package org.openimaj.examples.hardware.kinect;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.SwingUtilities;

import org.openimaj.demos.hardware.KinectDemo;
import org.openimaj.hardware.kinect.KinectException;
import org.openimaj.image.MBFImage;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.xuggle.XuggleVideoWriter;

/**
 * Record the output from a kinect to a file
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class KinectRecordingExample extends KeyAdapter implements VideoDisplayListener<MBFImage> {
	private KinectDemo demo;
	private XuggleVideoWriter writer;
	private boolean close = false;

	/**
	 * Default constructor
	 * 
	 * @throws KinectException
	 *             if there is a problem with the Kinect hardware
	 */
	public KinectRecordingExample() throws KinectException {
		demo = new KinectDemo(0);

		writer = new XuggleVideoWriter("kinect.mpg", demo.getCurrentFrame().getWidth(),
				demo.getCurrentFrame().getHeight(), 22);

		demo.getDisplay().addVideoListener(this);

		SwingUtilities.getRoot(demo.getDisplay().getScreen()).addKeyListener(this);
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		close = arg0.getKeyCode() == KeyEvent.VK_ESCAPE;
	}

	@Override
	public void afterUpdate(VideoDisplay<MBFImage> display) {
	}

	@Override
	public void beforeUpdate(MBFImage frame) {
		if (!close)
			writer.addFrame(frame);
	}

	/**
	 * Main method
	 * 
	 * @param args
	 *            ignored
	 * @throws KinectException
	 *             if there is a problem with the Kinect hardware
	 */
	public static void main(String[] args) throws KinectException {
		new KinectRecordingExample();
	}
}
