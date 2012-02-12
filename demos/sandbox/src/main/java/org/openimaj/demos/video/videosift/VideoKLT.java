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
import javax.swing.SwingUtilities;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.Transforms;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.tracking.klt.FeatureList;
import org.openimaj.video.tracking.klt.FeatureTable;
import org.openimaj.video.tracking.klt.KLTTracker;
import org.openimaj.video.tracking.klt.TrackingContext;

public class VideoKLT implements KeyListener, VideoDisplayListener<MBFImage> {
	
	private VideoCapture capture;
	private VideoDisplay<MBFImage> videoFrame;
	private KLTTracker tracker;
	private FeatureTable ft;
	private FeatureList fl;
	
	boolean firstFrame = true;
	private FImage oldFrame;
	private int frameNumber = 0;
	private int nFeatures = 150;
	private int nOriginalFoundFeatures = -1;
	public VideoKLT() throws Exception{
		capture = new VideoCapture(640, 480);
		videoFrame = VideoDisplay.createVideoDisplay(capture);
		videoFrame.addVideoListener(this);
		SwingUtilities.getRoot(videoFrame.getScreen()).addKeyListener(this);
		// videoFrame.getScreen().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		TrackingContext tc = new TrackingContext();
		fl = new FeatureList(nFeatures );
		ft = new FeatureTable(nFeatures);
		tracker = new KLTTracker(tc, fl);
		
		tc.setSequentialMode(true);
		tc.setWriteInternalImages(false);
		tc.setAffineConsistencyCheck(-1);  /* set this to 2 to turn on affine consistency check */
	}
	
	public boolean needsReset(){
		return this.firstFrame;
	}
	
	@Override
	public void afterUpdate(VideoDisplay<MBFImage> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeUpdate(MBFImage image) {
		FImage greyFrame = Transforms.calculateIntensityNTSC(image);
		if(needsReset()){

			frameNumber = 0;
			tracker.selectGoodFeatures(greyFrame);
			ft.storeFeatureList(fl, frameNumber);
			nOriginalFoundFeatures  = fl.countRemainingFeatures();
		}
		else{
			tracker.trackFeatures(oldFrame, greyFrame);
			if(fl.countRemainingFeatures() <= nOriginalFoundFeatures  * 0.5)
			{
				tracker.replaceLostFeatures(greyFrame);
				nOriginalFoundFeatures  = fl.countRemainingFeatures();
			}
			ft.storeFeatureList(fl, frameNumber);
		}
		fl.drawFeatures(image);
		this.oldFrame = greyFrame;
		this.firstFrame = false;
		this.frameNumber++;
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		System.out.println(e.getKeyChar());
		if(e.getKeyChar() == 'r'){
			this.firstFrame = true;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	public static void main(String args[]) throws Exception{
		new VideoKLT();
	}

}
