package org.openimaj.demos.video;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.SwingUtilities;

import org.openimaj.demos.video.utils.PolygonDrawingListener;
import org.openimaj.demos.video.utils.PolygonExtractionProcessor;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.matcher.FastBasicKeypointMatcher;
import org.openimaj.feature.local.matcher.consistent.ConsistentLocalFeatureMatcher2d;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.transforms.HomographyModel;
import org.openimaj.math.geometry.transforms.MatrixTransformProvider;
import org.openimaj.math.model.fit.RANSAC;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;

public class CaptureVideoSIFT implements KeyListener,VideoDisplayListener<MBFImage>{

	private VideoWithinVideo vwv;
	private PolygonDrawingListener polygonListener;
	private DoGSIFTEngine engine;
	private VideoDisplay<MBFImage> videoFrame;
	private MBFImage modelImage;
	private ConsistentLocalFeatureMatcher2d<Keypoint> matcher;
	private boolean ransacReader = false;

	public CaptureVideoSIFT(VideoWithinVideo videoWithinVideo) {
		this.vwv = videoWithinVideo;
		polygonListener = new PolygonDrawingListener();
		this.vwv.display.getScreen().addMouseListener(polygonListener);
		SwingUtilities.getRoot(this.vwv.display.getScreen()).addKeyListener(this);
		engine = new DoGSIFTEngine();
		engine.getOptions().setDoubleInitialImage(false);
		
		this.videoFrame = VideoDisplay.createOffscreenVideoDisplay(vwv.capture);
		this.videoFrame.addVideoListener(this);
	}
	
	@Override
	public void keyPressed(KeyEvent key) {
		if(key.getKeyCode() == KeyEvent.VK_SPACE) {
			this.videoFrame.togglePause();
		} 
		else if(key.getKeyChar() == 'r'){
			vwv.display.seek(0);
		}
		else if (key.getKeyChar() == 'c' && this.polygonListener.getPolygon().getVertices().size() > 2) {
			try {
				ransacReader  = false;
				Polygon p = this.polygonListener.getPolygon().clone();
				this.polygonListener.reset();
				modelImage = this.vwv.capture.getCurrentFrame().process(new PolygonExtractionProcessor<Float[],MBFImage>(p,RGBColour.BLACK));
				
				//configure the matcher
				HomographyModel model = new HomographyModel(3.0f);
				RANSAC<Point2d, Point2d> ransac = new RANSAC<Point2d, Point2d>(model, 1500, new RANSAC.ProbabilisticMinInliersStoppingCondition(0.01), true);
				matcher = new ConsistentLocalFeatureMatcher2d<Keypoint>(new FastBasicKeypointMatcher<Keypoint>(8));
				matcher.setFittingModel(ransac);

				DoGSIFTEngine engine = new DoGSIFTEngine();
				engine.getOptions().setDoubleInitialImage(false);

				FImage modelF = Transforms.calculateIntensityNTSC(modelImage);
				matcher.setModelFeatures(engine.findFeatures(modelF));
				vwv.display.seek(0);
				ransacReader  = true;
				
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {}

	@Override
	public void keyTyped(KeyEvent arg0) {}

	@Override
	public void afterUpdate(VideoDisplay<MBFImage> display) {
		if (ransacReader  && matcher != null && !videoFrame.isPaused()) {
			MBFImage capImg = videoFrame.getVideo().getCurrentFrame();
			LocalFeatureList<Keypoint> kpl = engine.findFeatures(Transforms.calculateIntensityNTSC(capImg));			
			if (matcher.findMatches(kpl)) {
				try {
					Polygon poly = modelImage.getBounds().transform(((MatrixTransformProvider) matcher.getModel()).getTransform().inverse()).asPolygon();
					
					this.vwv.targetArea = poly;
				} catch (RuntimeException e) {}
				
			} else {
				this.vwv.targetArea = null;
			}
		}
	}

	@Override
	public void beforeUpdate(MBFImage frame) {
		MBFImage frameWrite = frame;
		this.polygonListener.drawPoints(frameWrite);
		this.vwv.copyToCaptureFrame(frameWrite);
		
	}


}
