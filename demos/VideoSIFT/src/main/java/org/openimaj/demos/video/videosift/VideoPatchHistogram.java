package org.openimaj.demos.video.videosift;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.openimaj.feature.local.matcher.consistent.ConsistentKeypointMatcher;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.model.patch.HistogramPatchModel;
import org.openimaj.image.model.pixel.HistogramPixelModel;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;

public class VideoPatchHistogram implements KeyListener, VideoDisplayListener<MBFImage> {
	VideoCapture capture;
	VideoDisplay<MBFImage> videoFrame;
	JFrame modelFrame;
	JFrame matchFrame;
	MBFImage modelImage;

	ConsistentKeypointMatcher<Keypoint> matcher;
	private DoGSIFTEngine engine;
	private PolygonDrawingListener polygonListener;
	private boolean learnMode = false;
	private HistogramPatchModel hmodel;
	private boolean viewMode = false;
	private List<MBFImage> learningFrames;

	public VideoPatchHistogram() throws Exception {
		capture = new VideoCapture(640, 480);
		polygonListener = new PolygonDrawingListener();
		videoFrame = VideoDisplay.createVideoDisplay(capture);
		videoFrame.getScreen().addKeyListener(this);
		videoFrame.getScreen().getContentPane().addMouseListener(polygonListener);
		videoFrame.addVideoListener(this);
		engine = new DoGSIFTEngine();
		engine.getOptions().setDoubleInitialImage(false);
		hmodel = new HistogramPatchModel(10,10,10);
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
//			FImage greyFrame = Transforms.calculateIntensity(frame);
//			for(int y = 0; y < guess.height; y++){
//				for(int x = 0; x < guess.width; x++){
//					if(guess.pixels[y][x] < 0.1){
//						Float greyP = greyFrame.getPixel(x, y);
//						frame.setPixel(x, y, new Float[]{greyP,greyP,greyP});
//					}
//					
//				}
//			}
			frame.internalAssign(new MBFImage(new FImage[]{guess, guess, guess}));
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
		new VideoPatchHistogram();
	}
}
