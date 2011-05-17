package org.openimaj.demos.video.videosift;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.Transforms;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.quicktime.VideoCapture;
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
		videoFrame.getScreen().addKeyListener(this);
		
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
			try {
				tracker.selectGoodFeatures(greyFrame);
				ft.storeFeatureList(fl, frameNumber);
				nOriginalFoundFeatures  = fl.countRemainingFeatures();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else{
			try {
				tracker.trackFeatures(oldFrame, greyFrame);
				if(fl.countRemainingFeatures() <= nOriginalFoundFeatures  * 0.5)
				{
					tracker.replaceLostFeatures(greyFrame);
					nOriginalFoundFeatures  = fl.countRemainingFeatures();
				}
				ft.storeFeatureList(fl, frameNumber);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
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
