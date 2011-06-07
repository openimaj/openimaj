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

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.processing.face.parts.FacePipeline;
import org.openimaj.image.processing.face.parts.DetectedFace;
import org.openimaj.image.processing.face.parts.DetectedFace.DetectedFacePart;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.math.geometry.shape.Shape;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;

public class VideoFace implements KeyListener, VideoDisplayListener<MBFImage> {
	private VideoCapture capture;
	private VideoDisplay<MBFImage> videoFrame;

	private FacePipeline engine;
	private PolygonDrawingListener polygonListener;
	private float rescale;

	public VideoFace() throws Exception {
		capture = new VideoCapture(320, 240);
		engine = new FacePipeline();
		polygonListener = new PolygonDrawingListener();
		videoFrame = VideoDisplay.createVideoDisplay(capture);
		videoFrame.getScreen().addKeyListener(this);
		videoFrame.getScreen().addMouseListener(polygonListener);
		// videoFrame.getScreen().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		videoFrame.addVideoListener(this);
		this.rescale = 1.0f;
		
	}

	@Override
	public void keyPressed(KeyEvent key) {

	}

	@Override
	public void keyReleased(KeyEvent arg0) { }

	@Override
	public void keyTyped(KeyEvent arg0) { }

	public static void main(String [] args) throws Exception {		
		new VideoFace();
	}

	@Override
	public void afterUpdate(VideoDisplay<MBFImage> display) {

	}

	@Override
	public void beforeUpdate(MBFImage frame) {
		MBFImage resized = frame.process(new ResizeProcessor(1/rescale));
		LocalFeatureList<DetectedFace> faces = engine.extractFaces(Transforms.calculateIntensityNTSC(resized));
		for(DetectedFace face : faces){
			Shape transBounds = face.bounds.transform(TransformUtilities.scaleMatrix(rescale, rescale));
			frame.drawPolygon(transBounds.asPolygon(), RGBColour.RED);
			for(DetectedFacePart part: face.faceParts){
				frame.drawPoint(part.position, RGBColour.GREEN, 3);
			}
			
		}
		
	}
}
