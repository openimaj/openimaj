package org.openimaj.demos.video.videosift;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.matcher.consistent.ConsistentKeypointMatcher;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.shape.Shape;
import org.openimaj.math.geometry.transforms.HomographyModel;
import org.openimaj.math.geometry.transforms.MatrixTransformProvider;
import org.openimaj.math.model.fit.RANSAC;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.quicktime.VideoCapture;
import org.openimaj.video.tracking.klt.Feature;
import org.openimaj.video.tracking.klt.FeatureList;
import org.openimaj.video.tracking.klt.FeatureTable;
import org.openimaj.video.tracking.klt.KLTTracker;
import org.openimaj.video.tracking.klt.TrackingContext;


public class VideoKLTSIFT implements KeyListener, VideoDisplayListener<MBFImage> {
	enum Mode{
		TRACKING,LOOKING,NONE
	}
	private VideoCapture capture;
	private VideoDisplay<MBFImage> videoFrame;
	private KLTTracker tracker;
	private FeatureList fl;
	
	private FImage oldFrame;
	private int frameNumber = 0;
	private int nFeatures = 150;
	private int nOriginalFoundFeatures = -1;
	private DoGSIFTEngine engine;
	private PolygonDrawingListener polygonListener;
	private MBFImage modelImage;
	private JFrame modelFrame;
	private ConsistentKeypointMatcher<Keypoint> siftMatcher;
	private Mode  mode = Mode.NONE;
	private FeatureList oldFeatureList;
	private FeatureList initialFeatures;
	private Polygon initialShape;
	private Polygon polygonToDraw;
	public VideoKLTSIFT() throws Exception{
		capture = new VideoCapture(640, 480);
		polygonListener = new PolygonDrawingListener();
		videoFrame = VideoDisplay.createVideoDisplay(capture);
		videoFrame.getScreen().getContentPane().addMouseListener(polygonListener);
		videoFrame.addVideoListener(this);
		videoFrame.getScreen().addKeyListener(this);
		
		TrackingContext tc = new TrackingContext();
		fl = new FeatureList(nFeatures );
		tracker = new KLTTracker(tc, fl);
		tracker.setVerbosity(0);

		tc.setSequentialMode(true);
		tc.setWriteInternalImages(false);
		tc.setAffineConsistencyCheck(-1);  /* set this to 2 to turn on affine consistency check */
		
		engine = new DoGSIFTEngine();
		engine.getOptions().setDoubleInitialImage(false);
	}
	
	
	
	@Override
	public void afterUpdate(VideoDisplay<MBFImage> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeUpdate(MBFImage image) {
		FImage greyFrame = null;
		// If we are in looking mode, Use matcher to find a likely position every 5th frame
		if(this.mode == Mode.LOOKING){
			greyFrame = Transforms.calculateIntensityNTSC(image);
			Shape shape = findObject(greyFrame);
			if(shape == null) return;
			// If we find a likely position, init the tracker, we are now tracking
			initTracking(greyFrame, shape);
			this.mode = Mode.TRACKING;
			
		}
		// If we are tracking, attempt to track the points every frame
		else if(this.mode == Mode.TRACKING){
			greyFrame = Transforms.calculateIntensityNTSC(image);
			continueTracking(greyFrame);
			// If we don't track enough points, look again.
			if(fl.countRemainingFeatures() == 0 || fl.countRemainingFeatures() < nOriginalFoundFeatures  * 0.2)
			{
				this.mode = Mode.LOOKING;
				polygonToDraw = null;
			}
		}
		
		this.polygonListener.drawPoints(image);
		if(polygonToDraw!=null)
		{	
			image.drawPolygon(polygonToDraw, RGBColour.ORANGE);
//			image.drawPolygon(initialShape, RGBColour.RED);
		}
		fl.drawFeatures(image);
		this.oldFrame = greyFrame;
		
	}
	
	private Shape findObject(FImage capImg) {
		Shape sh = null;
		if (siftMatcher != null && !videoFrame.isPaused()) {
			LocalFeatureList<Keypoint> kpl = engine.findFeatures(capImg);
			if (siftMatcher.findMatches(kpl)) {
				sh = modelImage.getBounds().transform(((MatrixTransformProvider) siftMatcher.getModel()).getTransform().inverse());
			}
		}
		return sh;
	}



	public void initTracking(FImage greyFrame, Shape location){
		frameNumber = 0;
		try {
			tracker.getTc().setTargetArea(location);
			tracker.selectGoodFeatures(greyFrame);
			nOriginalFoundFeatures  = fl.countRemainingFeatures();
			initialFeatures = fl.clone();
			initialShape = location.asPolygon().clone();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void continueTracking(FImage greyFrame){
		try {
			oldFeatureList = fl.clone();
			tracker.trackFeatures(oldFrame, greyFrame);
			HomographyModel model = new HomographyModel(1.0f);
			RANSAC<Point2d, Point2d> ransac = new RANSAC<Point2d, Point2d>(model, 500, new RANSAC.PercentageInliersStoppingCondition(0.50), true);
			if(ransac.fitData(findAllMatchedPairs()))
				polygonToDraw = initialShape.transform(model.getTransform());
			else
				polygonToDraw = null;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.frameNumber++;
	}
	
	private List<? extends IndependentPair<Point2d, Point2d>> findAllMatchedPairs() {
		List<IndependentPair<Point2d, Point2d>> pairs = new ArrayList<IndependentPair<Point2d, Point2d>>();
		for(int i = 0; i < this.initialFeatures.features.length;i++){
			Feature oldFeature = this.initialFeatures.features[i];
			Feature newFeature = fl.features[i];
			if(oldFeature.val >= 0&& newFeature.val >=0){
				pairs .add(new IndependentPair<Point2d,Point2d>(oldFeature,newFeature));
			}
		}
		return pairs;
	}



	public Point2dImpl estimateMovement(){
		Feature[] oldFeatures = oldFeatureList.features;
		float sumX = 0;
		float sumY = 0;
		float total = 0;
		if(oldFeatures!=null){
			for(int i = 0; i < oldFeatures.length;i++){
				Feature oldFeature = oldFeatures[i];
				Feature newFeature = fl.features[i];
				if(oldFeature.val >= 0&& newFeature.val >=0){
					sumX += newFeature.x - oldFeature.x;
					sumY += newFeature.y - oldFeature.y;
					total +=1f;
				}
			}
			sumX/=total;
			sumY/=total;
			System.out.println("Average displacement: " + sumX + "," + sumY);
		}
		return new Point2dImpl(sumX,sumY);
	}

	@Override
	public void keyTyped(KeyEvent e) {
		
	}

	@Override
	public void keyPressed(KeyEvent key) {
		if(key.getKeyCode() == KeyEvent.VK_SPACE) {
			this.videoFrame.togglePause();
		} else if (key.getKeyChar() == 'c' && this.polygonListener.getPolygon().getVertices().size() > 2) {
			try {
				Polygon p = this.polygonListener.getPolygon().clone();
				this.polygonListener.reset();
				modelImage = capture.getCurrentFrame().process(new PolygonExtractionProcessor<Float[],MBFImage>(p,RGBColour.BLACK));

				if (modelFrame == null) {
					modelFrame = DisplayUtilities.display(modelImage, "model");
					modelFrame.addKeyListener(this);

					//move the frame
					Point pt = modelFrame.getLocation();
					modelFrame.setLocation(pt.x + this.videoFrame.getScreen().getWidth(), pt.y);

					//configure the matcher
					HomographyModel model = new HomographyModel(10.0f);
					RANSAC<Point2d, Point2d> ransac = new RANSAC<Point2d, Point2d>(model, 1500, new RANSAC.PercentageInliersStoppingCondition(0.20), true);
					siftMatcher = new ConsistentKeypointMatcher<Keypoint>(8,0);
					siftMatcher.setFittingModel(ransac);
				} else {
					DisplayUtilities.display(modelImage, modelFrame);
				}

				DoGSIFTEngine engine = new DoGSIFTEngine();
				engine.getOptions().setDoubleInitialImage(false);

				FImage modelF = Transforms.calculateIntensityNTSC(modelImage);
				siftMatcher.setModelFeatures(engine.findFeatures(modelF));
				mode = Mode.LOOKING;
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	public static void main(String args[]) throws Exception{
		new VideoKLTSIFT();
	}

}
