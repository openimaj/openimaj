package org.openimaj.demos.video.videosift;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.feature.local.matcher.MatchingUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.shape.Shape;
import org.openimaj.math.geometry.transforms.HomographyModel;
import org.openimaj.math.model.fit.RANSAC;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.quicktime.VideoCapture;
import org.openimaj.video.tracking.klt.Feature;
import org.openimaj.video.tracking.klt.FeatureList;
import org.openimaj.video.tracking.klt.KLTTracker;
import org.openimaj.video.tracking.klt.TrackingContext;

import Jama.Matrix;


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
	private int nFeatures = 50;
	private int nOriginalFoundFeatures = -1;
	private DoGSIFTEngine engine;
	private PolygonDrawingListener polygonListener;
	private MBFImage modelImage;
	private Mode  mode = Mode.NONE;
	private FeatureList initialFeatures;
	private Polygon initialShape;
	public VideoKLTSIFT() throws Exception{
		capture = new VideoCapture(640, 480);
		polygonListener = new PolygonDrawingListener();
		videoFrame = VideoDisplay.createVideoDisplay(capture);
		videoFrame.getScreen().getContentPane().addMouseListener(polygonListener);
		videoFrame.addVideoListener(this);
		videoFrame.getScreen().addKeyListener(this);
		
		reinitTracker();
	}
	
	
	
	@Override
	public void afterUpdate(VideoDisplay<MBFImage> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeUpdate(MBFImage image) {
		FImage greyFrame = null;
		// If we are in looking mode, Use matcher to find a likely position every 5th frame
//		if(this.mode == Mode.LOOKING && (lastLookTime == -1 || System.currentTimeMillis() - lastLookTime  > 1000)){
//			greyFrame = Transforms.calculateIntensityNTSC(image);
//			Shape shape = findObject(greyFrame);
//			if(shape == null) return;
//			// If we find a likely position, init the tracker, we are now tracking
//			initTracking(greyFrame, shape);
//			this.mode = Mode.TRACKING;
//			lastLookTime = System.currentTimeMillis();
//		}
		// If we are tracking, attempt to track the points every frame
		if(this.mode == Mode.TRACKING){
			greyFrame = Transforms.calculateIntensityNTSC(image);
			continueTracking(greyFrame);
			// If we don't track enough points, look again.
			if(fl.countRemainingFeatures() == 0 || fl.countRemainingFeatures() < nOriginalFoundFeatures  * 0.2)
			{
				this.mode = Mode.NONE;
				reinitTracker();
			}
//			else if(fl.countRemainingFeatures() < nOriginalFoundFeatures  * 0.8){
//				initTracking(greyFrame,polygonToDraw);
//			}
		}
		
		this.polygonListener.drawPoints(image);
		if(this.initialShape!=null){
			image.drawPolygon(initialShape, RGBColour.RED);
		}
		if(this.initialFeatures != null){
			image.internalAssign(MatchingUtilities.drawMatches(image, this.findAllMatchedPairs(), RGBColour.WHITE));
			Matrix esitmatedModel = this.estimateModel();
			if(esitmatedModel!=null)
			{
				Polygon newPolygon = initialShape.transform(esitmatedModel);
				image.drawPolygon(newPolygon, RGBColour.GREEN);
				if(fl.countRemainingFeatures() < nOriginalFoundFeatures  * 0.5){
					reinitTracker();
					initTracking(greyFrame,newPolygon);
				}
			}
			estimateMovement();
		}
		
		
		

		this.oldFrame = greyFrame;
		
	}
	
//	private Shape findObject(FImage capImg) {
//		Shape sh = null;
//		if (siftMatcher != null && !videoFrame.isPaused()) {
//			LocalFeatureList<Keypoint> kpl = engine.findFeatures(capImg);
//			if (siftMatcher.findMatches(kpl)) {
//				sh = modelImage.getBounds().transform(((MatrixTransformProvider) siftMatcher.getModel()).getTransform().inverse());
//			}
//		}
//		return sh;
//	}



	private void reinitTracker() {
		TrackingContext tc = new TrackingContext();
		fl = new FeatureList(nFeatures );
		tracker = new KLTTracker(tc, fl);
		tracker.setVerbosity(0);

		tc.setSequentialMode(true);
		tc.setWriteInternalImages(false);
		tc.setAffineConsistencyCheck(-1);  /* set this to 2 to turn on affine consistency check */
		this.initialFeatures = null;
		this.initialShape = null;
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
			tracker.trackFeatures(oldFrame, greyFrame);
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.frameNumber++;
	}
	
	private Matrix estimateModel() {
		List<? extends IndependentPair<Point2d, Point2d>> pairs = findAllMatchedPairs();
		HomographyModel model = new HomographyModel(20.0f);
//		model.estimate(pairs);
		RANSAC<Point2d,Point2d> fitter = new RANSAC<Point2d,Point2d>(model,1500,new RANSAC.PercentageInliersStoppingCondition(0.5),false);
		if(!fitter.fitData(pairs))
			return null;

		model.getTransform().print(5, 5);
		return model.getTransform();
	}



	private List<IndependentPair<Point2d, Point2d>> findAllMatchedPairs() {
		List<IndependentPair<Point2d, Point2d>> pairs = new ArrayList<IndependentPair<Point2d, Point2d>>();
		for(int i = 0; i < this.initialFeatures.features.length;i++){
			Feature oldFeature = this.initialFeatures.features[i].clone();
			Feature newFeature = fl.features[i].clone();
			if(oldFeature.val >= 0 && newFeature.val >=0){
				pairs .add(new IndependentPair<Point2d,Point2d>(oldFeature,newFeature));
			}
		}
		return pairs;
	}



	public Point2dImpl estimateMovement(){
		Feature[] oldFeatures = this.initialFeatures.features;
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
				modelImage = capture.getCurrentFrame();
				FImage greyFrame = Transforms.calculateIntensityNTSC(modelImage);
				this.initTracking(greyFrame, p);
				this.oldFrame = greyFrame;
				mode = Mode.TRACKING;
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
		else if(key.getKeyChar() == 'r'){
			reinitTracker();
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
