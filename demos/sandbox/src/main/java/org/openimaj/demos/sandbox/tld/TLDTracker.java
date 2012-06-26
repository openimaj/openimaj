package org.openimaj.demos.sandbox.tld;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.video.Video;
import org.openimaj.video.tracking.klt.Feature;
import org.openimaj.video.tracking.klt.FeatureList;
import org.openimaj.video.tracking.klt.KLTTracker;
import org.openimaj.video.tracking.klt.PyramidSet;
import org.openimaj.video.tracking.klt.TrackingContext;

public class TLDTracker {
	private TLDOptions opt;
	public KLTTracker tracker;
	Rectangle currentBoundingBox;
	private FImage currentImage;

	public TLDTracker(TLDOptions opt) {
		this.opt = opt;
	}
	
	/**
	 * initialise the tracker and the classifiers
	 */
	public void init(FImage image, Rectangle boundingBox) {
		// Initialise a KLT tracker with this image and this bounding box
		TrackingContext tc = new TrackingContext();
		tc.setSequentialMode(true);
		tc.setWindowWidth((Integer) opt.tracker.v("windowsize"));
		tc.setWindowHeight((Integer) opt.tracker.v("windowsize"));
		tc.setTargetArea(boundingBox);
		FeatureList fl = new FeatureList((Integer) opt.tracker.v("nfeatures"));
		tracker = new KLTTracker(tc, fl);
		tracker.setVerbosity(0);
		tracker.setNorm(false);
		image.multiplyInplace(255f);
		tracker.selectGoodFeatures(image);
		
		this.currentBoundingBox = boundingBox.clone();
		this.currentImage = image;
	}
	
	/**
	 * Process the next frame using the internal video
	 * @param frame 
	 */
	public void processFrame(FImage frame) {
		if(tracker == null){
			// We are in detection mode
			return;
		}
		frame.multiplyInplace(255f);
		// Perform forward backward tracking
		// Get the previous frame if it exists
		PyramidSet currentFramePyr = tracker.getTrackingContext().previousPyramidSet();
		if(currentFramePyr == null){
			currentFramePyr = new PyramidSet(currentImage,tracker.getTrackingContext());
		}
		// The current frame's pyramid
		PyramidSet nextFramePyr = new PyramidSet(frame,tracker.getTrackingContext());
		// Track the features from current to nextframe
		FeatureList currentFeatures = tracker.getFeatureList().clone();
		tracker.trackFeatures(currentImage, frame,currentFramePyr,nextFramePyr);
		FeatureList currentToNextFeatures = tracker.getFeatureList().clone();
		// ... and from next back to current frame
		tracker.trackFeatures(frame, currentImage,nextFramePyr,currentFramePyr);
		FeatureList nextToCurrentFeatures = tracker.getFeatureList().clone();
		tracker.getTrackingContext().setPreviousPyramid(nextFramePyr);
		
//		predictNewBoundingBox(currentFeatures, currentToNextFeatures);
//		TLDFrontBackMediaFlowTracker tldfbmf = new TLDFrontBackMedianFlowTracker(currentFeatures,currentToNextFeatures,nextToCurrentFeatures,this.currentBoundingBox);
		if(this.currentBoundingBox == null){
			tracker = null;
			return;
		}			
		tracker.getTrackingContext().setTargetArea(this.currentBoundingBox);
		tracker.replaceLostFeatures(frame);
	}
	
	public List<Point2d> getTrackerPoints()
	{
		List<Point2d> points = new ArrayList<Point2d>();
		for (Feature f : tracker.getFeatureList().features) {
			if(f.val >= 0)points.add(f);
		}
		return points;
	}
	
	// Use matching pairs from the current bounding box to a new location to predict a new bounding box
	private void predictNewBoundingBox(FeatureList fl1, FeatureList fl2) {
		PriorityQueue<Float> xList = new PriorityQueue<Float>();
		PriorityQueue<Float> yList = new PriorityQueue<Float>();
		
		for(int i = 0; i < fl1.features.length;i++){
			Feature oldFeature = fl1.features[i];
			Feature newFeature = fl2.features[i];
			if(oldFeature.val >= 0 && newFeature.val >=0){
				xList.offer(newFeature.x - oldFeature.x);
				yList.offer(newFeature.y - oldFeature.y);
			}
		}
		if(xList.size() == 0 || yList.size() == 0) {
			this.currentBoundingBox = null;
			return;
		}
		float dx = median(xList);
		float dy = median(yList);
		xList.clear();
		
		for(int i = 0; i < fl1.features.length;i++){
			Feature oldFeatureI = fl1.features[i];
			Feature newFeatureI = fl2.features[i];
			if(oldFeatureI.val < 0 || newFeatureI.val < 0){
				continue;
			}
			for(int j = i+1; j < fl2.features.length;j++){
				Feature oldFeatureJ = fl1.features[j];
				Feature newFeatureJ = fl2.features[j];
				if(oldFeatureJ.val < 0 || newFeatureJ.val < 0){
					continue;
				}
				float d1 = (float) Line2d.distance(oldFeatureI.x,oldFeatureI.y,oldFeatureJ.x,oldFeatureJ.y);
				float d2 = (float) Line2d.distance(newFeatureI.x,newFeatureI.y,newFeatureJ.x,newFeatureJ.y);
				
				xList.offer(d2/d1);
			}
		}
		float s = median(xList);
		
		float s1 = 0.5f*(s)*this.currentBoundingBox.width;
		float s2 = 0.5f*(s)*this.currentBoundingBox.height;
		
		float newX = this.currentBoundingBox.x - s1 + dx;
		float newY = this.currentBoundingBox.y - s2 + dy;
		float newW = this.currentBoundingBox.width + s1 + dx;
		float newH = this.currentBoundingBox.height + s2 + dy;
		
		this.currentBoundingBox = new Rectangle(newX,newY,newW,newH);
	}

	private static float median(PriorityQueue<Float> dxList) {
		int len = dxList.size() ;
		if(len == 0) return Float.NaN;
		int mid = len/2;
		// even: mid + (mid + 1)/2
		float v1 = 0;
		for (int i = 0; i < mid; i++) v1 = dxList.poll();
		if(len % 2 == 0){
			return (v1 + dxList.poll())/2;
		}else{
			return dxList.poll();
		}
	}
	
	public static void main(String[] args) {
		PriorityQueue<Float> q = new PriorityQueue<Float>();
		q.offer(1f);
		q.offer(4f);
		q.offer(3f);
		q.offer(2f);
		System.out.println(median(q));
	}
}
