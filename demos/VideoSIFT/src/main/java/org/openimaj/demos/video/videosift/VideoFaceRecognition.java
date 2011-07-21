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
package org.openimaj.demos.video.videosift;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.processing.face.alignment.AffineAligner;
import org.openimaj.image.processing.face.feature.comparison.LtpDtFeatureComparator;
import org.openimaj.image.processing.face.feature.ltp.LtpDtFeature;
import org.openimaj.image.processing.face.feature.ltp.TruncatedWeighting;
import org.openimaj.image.processing.face.keypoints.FKEFaceDetector;
import org.openimaj.image.processing.face.keypoints.KEDetectedFace;
import org.openimaj.image.processing.face.recognition.SimpleKNNRecogniser;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.capture.VideoCapture;

public class VideoFaceRecognition implements KeyListener {
	private VideoCapture capture;
	private VideoDisplay<MBFImage> videoFrame;

	private FKEFaceDetector engine;
	private SimpleKNNRecogniser<LtpDtFeature, KEDetectedFace> recogniser;

	public VideoFaceRecognition() throws Exception {
		capture = new VideoCapture(320, 240);
		engine = new FKEFaceDetector();
		videoFrame = VideoDisplay.createVideoDisplay(capture);
		SwingUtilities.getRoot(videoFrame.getScreen()).addKeyListener(this);
		recogniser = new SimpleKNNRecogniser<LtpDtFeature, KEDetectedFace>(
				new LtpDtFeature.Factory<KEDetectedFace>(new AffineAligner(), new TruncatedWeighting()), 
				new LtpDtFeatureComparator(), 
				1);
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent key) {
		if(key.getKeyCode() == KeyEvent.VK_SPACE) {
			this.videoFrame.togglePause();
		} else if (key.getKeyChar() == 'c') {
			if (!this.videoFrame.isPaused())
				this.videoFrame.togglePause();
			
			String person = JOptionPane.showInputDialog(this.videoFrame.getScreen(), "", "", JOptionPane.QUESTION_MESSAGE);
			FImage image = Transforms.calculateIntensityNTSC(this.videoFrame.getVideo().getCurrentFrame());
			
			List<KEDetectedFace> faces = engine.detectFaces(image);
			if (faces.size() == 1) {
				recogniser.addInstance(person, faces.get(0));
			} else {
				System.out.println("Wrong number of faces found");
			}
			
			this.videoFrame.togglePause();
		} else if (key.getKeyChar() == 'q') {
			if (!this.videoFrame.isPaused())
				this.videoFrame.togglePause();
			
			FImage image = Transforms.calculateIntensityNTSC(this.videoFrame.getVideo().getCurrentFrame());
			
			List<KEDetectedFace> faces = engine.detectFaces(image);
			if (faces.size() == 1) {
				System.out.println("Looks like: " + recogniser.queryBestMatch(faces.get(0)));
			} else {
				System.out.println("Wrong number of faces found");
			}
			
			this.videoFrame.togglePause();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	public static void main(String [] args) throws Exception {		
		new VideoFaceRecognition();
	}
}
