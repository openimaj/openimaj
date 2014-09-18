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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.video.Video;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.capture.VideoCapture;

/**
 * Example showing how to play a video and save single frame snapshots on a
 * button-press.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class VideoSnapshotExample {
	private static void createUI(final Video<MBFImage> video) {
		// create the window
		final JFrame frame = new JFrame("Snapshot Example");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// setup the video
		final JPanel videoPanel = new JPanel();
		final VideoDisplay<MBFImage> display = VideoDisplay.createVideoDisplay(video, videoPanel);
		frame.add(videoPanel, BorderLayout.CENTER);

		// add the button
		final JButton button = new JButton("Take Snapshot");
		final String SNAPSHOT_COMMAND = "snapshot";
		button.setActionCommand(SNAPSHOT_COMMAND);
		frame.getContentPane().add(button, BorderLayout.SOUTH);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				if (evt.getActionCommand() == SNAPSHOT_COMMAND) {
					// this gets called if the button is pressed

					// pause the video
					display.setMode(VideoDisplay.Mode.PAUSE);

					// display a file save dialog
					final JFileChooser saveFile = new JFileChooser();
					if (saveFile.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
						// if a file was selected write the image
						try {
							// we're just going to add .jpg to the filename and
							// save it... should be much more intelligent here
							// in reality...
							File outfile = saveFile.getSelectedFile();
							outfile = new File(outfile.getParentFile(), outfile.getName() + ".jpg");

							ImageUtilities.write(video.getCurrentFrame(), outfile);
						} catch (final IOException ioe) {
							// display an error if the file couldn't be saved
							JOptionPane.showMessageDialog(null, "Unable to save file.");
						}
					}

					// start the video playing again
					display.setMode(VideoDisplay.Mode.PLAY);
				}
			}
		});

		// prepare the window for display
		frame.pack();

		// and display it
		frame.setVisible(true);
	}

	/**
	 * Run the example
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			// Open the video. Here we're using the default webcam, but this
			// could
			// be any type of video, such as a XuggleVideo which reads from a
			// file.
			final Video<MBFImage> video = new VideoCapture(320, 240);

			// create and display the UI
			createUI(video);
		} catch (final IOException e) {
			// an error occured
			JOptionPane.showMessageDialog(null, "Unable to open video.");
		}
	}
}
