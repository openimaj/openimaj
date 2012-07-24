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

import javax.swing.SwingUtilities;

import org.openimaj.demos.video.utils.PolygonDrawingListener;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.FaceDetector;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.image.processing.face.detection.keypoints.FKEFaceDetector;
import org.openimaj.image.processing.face.detection.keypoints.FacialKeypoint;
import org.openimaj.image.processing.face.detection.keypoints.KEDetectedFace;
import org.openimaj.image.renderer.MBFImageRenderer;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.shape.Shape;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;

public class VideoFace implements VideoDisplayListener<MBFImage>, KeyListener {
	private VideoCapture capture;
	private VideoDisplay<MBFImage> videoFrame;

	private FaceDetector<DetectedFace, FImage> innerEngine;
	private FKEFaceDetector engine;

	private PolygonDrawingListener polygonListener;

	boolean findKeypoints = false;

	public VideoFace() throws Exception {
		capture = new VideoCapture(320, 240);

		innerEngine = new HaarCascadeDetector();
		engine = new FKEFaceDetector(innerEngine);

		polygonListener = new PolygonDrawingListener();
		videoFrame = VideoDisplay.createVideoDisplay(capture);
		videoFrame.getScreen().addMouseListener(polygonListener);
		videoFrame.addVideoListener(this);
		SwingUtilities.getRoot(videoFrame.getScreen()).addKeyListener(this);
	}

	@Override
	public void afterUpdate(VideoDisplay<MBFImage> display) {

	}

	@Override
	public synchronized void beforeUpdate(MBFImage frame) {
		List<? extends DetectedFace> faces = null;
		if (findKeypoints) {
			faces = engine
					.detectFaces(Transforms.calculateIntensityNTSC(frame));
		} else {
			faces = innerEngine.detectFaces(Transforms
					.calculateIntensityNTSC(frame));
		}

		if (faces.size() > 0) {
			Rectangle r = faces.get(0).getBounds();
			((HaarCascadeDetector) innerEngine)
					.setMinSize((int) (r.width * 0.9));
		} else {
			((HaarCascadeDetector) innerEngine).setMinSize(1);
		}

		for (DetectedFace face : faces) {
			final Shape bounds = face.getBounds();

			MBFImageRenderer renderer = frame.createRenderer();
			renderer.drawPolygon(bounds.asPolygon(), RGBColour.RED);

			if (findKeypoints) {
				for (FacialKeypoint kp : ((KEDetectedFace) face).getKeypoints()) {
					Point2d pt = kp.position.clone();
					pt.translate((float) bounds.minX(), (float) bounds.minY());

					renderer.drawPoint(pt, RGBColour.GREEN, 3);
				}
			}
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public synchronized void keyPressed(KeyEvent e) {
		if (e.getKeyChar() == 't') {
			findKeypoints = !findKeypoints;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {

	}

	public static void main(String[] args) throws Exception {
		new VideoFace();
	}
}
