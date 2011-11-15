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
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.openimaj.demos.video.utils.PolygonDrawingListener;
import org.openimaj.demos.video.utils.PolygonExtractionProcessor;
import org.openimaj.feature.local.matcher.consistent.ConsistentLocalFeatureMatcher2d;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.model.pixel.HistogramPixelModel;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;

public class VideoPixelHistogram implements KeyListener, VideoDisplayListener<MBFImage> {
	VideoCapture capture;
	VideoDisplay<MBFImage> videoFrame;
	JFrame modelFrame;
	JFrame matchFrame;
	MBFImage modelImage;

	ConsistentLocalFeatureMatcher2d<Keypoint> matcher;
	private DoGSIFTEngine engine;
	private PolygonDrawingListener polygonListener;
	private boolean learnMode = false;
	private HistogramPixelModel hmodel;
	private boolean viewMode = false;
	private List<MBFImage> learningFrames;

	public VideoPixelHistogram() throws Exception {
		capture = new VideoCapture(640, 480);
		polygonListener = new PolygonDrawingListener();
		videoFrame = VideoDisplay.createVideoDisplay(capture);
		SwingUtilities.getRoot(videoFrame.getScreen()).addKeyListener(this);
		SwingUtilities.getRoot(videoFrame.getScreen()).addMouseListener(polygonListener);
		videoFrame.addVideoListener(this);
		engine = new DoGSIFTEngine();
		engine.getOptions().setDoubleInitialImage(false);
		hmodel = new HistogramPixelModel(10,10,10);
		this.learningFrames = new ArrayList<MBFImage>();
	}

	@Override
	public void afterUpdate(VideoDisplay<MBFImage> display) {
		
	}

	@Override
	public void beforeUpdate(MBFImage frame) {
		if(learnMode){
			System.out.println("Adding frame");
			if(this.learningFrames.size()>5)
				this.learningFrames.remove(0);
			this.learningFrames.add(frame.process(new PolygonExtractionProcessor<Float[],MBFImage>(this.polygonListener.getPolygon(),RGBColour.BLACK)));
			
		}
		if(viewMode){
			FImage guess = this.hmodel.classifyImage(frame).normalise();
			FImage greyFrame = Transforms.calculateIntensity(frame);
			for(int y = 0; y < guess.height; y++){
				for(int x = 0; x < guess.width; x++){
					if(guess.pixels[y][x] < 0.1){
						Float greyP = greyFrame.getPixel(x, y);
						frame.setPixel(x, y, new Float[]{greyP,greyP,greyP});
					}
					
				}
			}
		}
		this.polygonListener.drawPoints(frame);
		
	}

	@Override
	public void keyPressed(KeyEvent key) {
		if(key.getKeyCode() == KeyEvent.VK_SPACE) {
			this.videoFrame.togglePause();
		}
		if (key.getKeyChar() == 'v' ) {
				this.viewMode = !this.viewMode ;
		}
		if (key.getKeyChar() == 'c' && this.polygonListener.getPolygon().getVertices().size()>2) {
			try {
				if(!this.learnMode)
				{
					this.learnMode  = true;
				}
				else{
					this.polygonListener.reset();
					this.hmodel.learnModel(this.learningFrames.toArray(new MBFImage[this.learningFrames.size()]));
					this.learnMode = false;
				}

			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	public static void main(String args[]) throws Exception{
		new VideoPixelHistogram();
	}
}
