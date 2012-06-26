package org.openimaj.demos.sandbox.tld;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.math.stat.descriptive.rank.Median;
import org.openimaj.image.FImage;
import org.openimaj.image.analysis.algorithm.TemplateMatcher;
import org.openimaj.image.analysis.algorithm.TemplateMatcher.Mode;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.video.tracking.klt.Feature;
import org.openimaj.video.tracking.klt.FeatureList;

/**
 * An implementation of front back median flow which basically says
 * "A feature tracks well if we found it going from A -> B but also from B - A.
 * 
 * The media flow part says
 * "The features which tracked well have the lowest median euclidian distance once 
 * gone from A -> B and back to B -> A.
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TLDFrontBackMedianFlowTracker {
	private static final Median median = new Median();
	private static final int WINDOW_SIZE = 10;
	private static final int HALF_WINDOW_SIZE = WINDOW_SIZE/2;
//	private static PatchMatcher normalisedCrossCorrelation = new PatchMatcher.NORM_CORRELATION_COEFFICIENT();
	private FBFeatureSet[] fbFeatures;
	private Rectangle predictedBox;
	private Rectangle currentBoundingBox;
	private boolean valid = false;
	private Object confidence;
	private Rectangle imageBox;
	private TLDOptions opts;
	private double fbMedian;
	private double nccMedian;
	/**
	 * The features are tracked as follows:
	 * 
	 * CurrentImage -> NextImage -> CurrentImage
	 * 
	 * 2 scores are calculated for each successfully tracked feature:
	 * {@link FBFeatureSet#forwardBackDistance} == the euclidian distance between the original feature and the feature tracked from the next image to the current image
	 * {@link FBFeatureSet#normalisedCrossCorrelation} == the normalised cross correlation between the feature in this image tracked to the next image
	 * 
	 * @param current The current frame, current features originate here 
	 * @param next The next frame, current features are tracked to, and then back from points in this image
	 * @param currentFeatures
	 * @param currentToNextFeatures
	 * @param nextToCurrentFeatures
	 * @param currentBoundingBox
	 */
	public TLDFrontBackMedianFlowTracker(FImage current, FImage next, FeatureList currentFeatures,FeatureList currentToNextFeatures,FeatureList nextToCurrentFeatures, Rectangle currentBoundingBox,TLDOptions opts) {
		this.opts = opts;
		this.valid  = false;
		this.confidence = null;
		this.predictedBox = null;
		this.imageBox = current.getBounds();
		
		if(currentBoundingBox == null) return;
		List<FBFeatureSet> fbFeatures = new ArrayList<FBFeatureSet>();
		double[] fbs = new double[currentToNextFeatures.features.length];
		double[] nccs = new double[currentToNextFeatures.features.length];
		int valueIndex = 0;
		
		Iterator<Feature> currentFeaturesItr = currentFeatures.iterator();
		Iterator<Feature> currentToNextFeaturesItr = currentToNextFeatures.iterator();
		
		
		for (Feature featEnd : nextToCurrentFeatures) {
			Feature featStart = currentFeaturesItr.next();
			Feature featMiddle = currentToNextFeaturesItr.next();
			
			FBFeatureSet newFeature = testNewFeature(current,next,featStart,featMiddle,featEnd);
			if(newFeature == null) continue;
			fbs[valueIndex] = newFeature.forwardBackDistance;
			nccs[valueIndex] = newFeature.normalisedCrossCorrelation;
			
			valueIndex ++;
			fbFeatures.add(newFeature);
		}
		
		updateMedians(fbs,nccs,valueIndex);
		// If FB is bad stop early
		if((Boolean) this.opts.control.get("maxbbox") && fbMedian > 10){
			this.predictedBox = null;
			return;
		}
		pruneFeatures(fbFeatures);
		this.fbFeatures = fbFeatures.toArray(new FBFeatureSet[fbFeatures.size()]);
		this.currentBoundingBox = currentBoundingBox;
		this.calculateBoundingBox();
		if(!this.validPredictedBox()){
			this.predictedBox = null;
			return;
		}
	}
	/**
	 * @param current
	 * @param next
	 * @param features
	 * @param currentBoundingBox 
	 * @param opts 
	 */
	public TLDFrontBackMedianFlowTracker(FImage current, FImage next,FBFeatureSet[] features, Rectangle currentBoundingBox,TLDOptions opts) {
		this.opts = opts;
		this.valid  = false;
		this.confidence = null;
		this.predictedBox = null;
		this.imageBox = current.getBounds();
		
		if(currentBoundingBox == null) return;
		List<FBFeatureSet> fbFeatures = new ArrayList<FBFeatureSet>();
		double[] fbs = new double[features.length];
		double[] nccs = new double[features.length];
		int valueIndex = 0;
		
		for (FBFeatureSet featSet : features) {
			Feature featStart = featSet.start;
			Feature featMiddle = featSet.middle;
			Feature featEnd = featSet.end;
			
			FBFeatureSet newFeature = testNewFeature(current,next,featStart,featMiddle,featEnd);
			if(newFeature == null) continue;
			fbs[valueIndex] = newFeature.forwardBackDistance;
			nccs[valueIndex] = newFeature.normalisedCrossCorrelation;
			System.out.println("Expected vs actual FBDistance: " + featSet.forwardBackDistance + " vs " + newFeature.forwardBackDistance);
			System.out.println("Expected vs actual NCC: " + featSet.normalisedCrossCorrelation + " vs " + newFeature.normalisedCrossCorrelation);
			
			valueIndex ++;
			fbFeatures.add(newFeature);
		}
		updateMedians(fbs,nccs,valueIndex);
		// If FB is bad stop early
		if((Boolean) this.opts.control.get("maxbbox") && fbMedian > 10){
			this.predictedBox = null;
			return;
		}
		pruneFeatures(fbFeatures);
		this.fbFeatures = fbFeatures.toArray(new FBFeatureSet[fbFeatures.size()]);
		this.currentBoundingBox = currentBoundingBox;
		this.calculateBoundingBox();
		if(!this.validPredictedBox()){
			this.predictedBox = null;
			return;
		}
		// the v
		
		
	}
	
	private void updateMedians(double[] fbs, double[] nccs, int valueIndex) {
		Arrays.sort(fbs, 0, valueIndex);
		Arrays.sort(nccs, 0, valueIndex);
		
		fbMedian = median.evaluate(fbs, 0, valueIndex);
		nccMedian = median.evaluate(nccs, 0, valueIndex);
	}
	private boolean validPredictedBox() {
		return this.predictedBox != null && this.predictedBox.isInside(imageBox);
	}
	private void pruneFeatures(List<FBFeatureSet> fbFeatures) {
		
		for (Iterator<FBFeatureSet> iterator = fbFeatures.iterator(); iterator.hasNext();) {
			FBFeatureSet fbFeature = iterator.next();
			boolean goodFBDistance = fbFeature.forwardBackDistance <= fbMedian;
			boolean goodNCC = fbFeature.normalisedCrossCorrelation >= nccMedian;
			System.out.println(goodFBDistance + " && " + goodNCC);
			if(!goodFBDistance || !goodNCC) 
				iterator.remove();
		}
	}
	private FBFeatureSet testNewFeature(FImage current,FImage next,Feature featStart, Feature featMiddle,Feature featEnd) {
		if(featEnd.val < 0 ){
			// this feature didn't survive going from A to B and back to A, it must be unstable, throw it away
			return null;
		}
		FBFeatureSet newFeature = new FBFeatureSet(featStart,featMiddle,featEnd);
		// Correlation in a window centered around the feature(x,y)
		int featStartX = (int) (featStart.x - HALF_WINDOW_SIZE); featStartX = featStartX < 0 ? 0 : featStartX;
		int featStartY = (int) (featStart.y - HALF_WINDOW_SIZE); featStartY = featStartY < 0 ? 0 : featStartY;
		int featMiddleX = (int) (featMiddle.x - HALF_WINDOW_SIZE); featMiddleX = featMiddleX < 0 ? 0 : featMiddleX;
		int featMiddleY = (int) (featMiddle.y - HALF_WINDOW_SIZE); featMiddleY = featMiddleY < 0 ? 0 : featMiddleY;
		
		
		newFeature.forwardBackDistance = (float) Line2d.distance(featStart.x, featStart.y, featEnd.x, featEnd.y);		
		newFeature.normalisedCrossCorrelation = Mode.NORM_CORRELATION.computeMatchScore(current.pixels, featStartX, featStartY, next.pixels, featMiddleX, featMiddleY, WINDOW_SIZE, WINDOW_SIZE);
		return newFeature;
	}
	
	/**
	 * @return return the predicted bounding box
	 */
	public Rectangle predictedBox(){
		return this.predictedBox;
	}
	
	private Rectangle calculateBoundingBox(){
		int fbsize = fbFeatures.length;
		if(fbsize == 0){
			return null;
		}
		if(this.predictedBox != null){
			return this.predictedBox;
		}
		// create the workspace used by both the median position and the media movement
		int wsSize = fbsize < 2 ? fbsize * 2 : fbsize*fbsize;
		double[] workspace = new double[wsSize];
		Point2d medianDelta = medianDelta(workspace);
		double medianRatio = medianDistanceRatio(workspace);
		float s1 = (float) (0.5*(medianRatio-1)*this.currentBoundingBox.width);
		float s2 = (float) (0.5*(medianRatio-1)*this.currentBoundingBox.height);
		this.predictedBox = new Rectangle(
				this.currentBoundingBox.x - s1 + medianDelta.getX(), 
				this.currentBoundingBox.y - s2 + medianDelta.getY(),
				this.currentBoundingBox.width + 2 * s1, 
				this.currentBoundingBox.height + 2 * s2
		);
		return this.predictedBox;
	}
	/**
	 * Find the median ratio of the pairwise distances of points between the current image and the next
	 * so:
	 * d1 = pairwiseEuclidian(currentImagePoints)
	 * d2 = pairwiseEuclidian(currentImagePoints)
	 * ratios = d2./d1
	 * @param workspace
	 * @return median(ratios)
	 */
	private double medianDistanceRatio(double[] workspace) {
		if(workspace.length==1){
			return 0;
		}
		int k = 0;
		for (int i = 0; i < this.fbFeatures.length; i++) {
			FBFeatureSet fs1 = this.fbFeatures[i];
			for (int j = i+1; j < this.fbFeatures.length; j++) {
				FBFeatureSet fs2 = this.fbFeatures[j];
				double d1 = Line2d.distance(fs1.start, fs2.start);
				double d2 = Line2d.distance(fs1.middle, fs2.middle);
				workspace[k++] = d2/d1;
			}
		}
		
		Arrays.sort(workspace, 0, k);
		return median.evaluate(workspace, 0, k);
	}
	/**
	 * Find the median delta between good points
	 * @param workspace 
	 * @return
	 */
	private Point2d medianDelta(double[] workspace) {
		
		int fbSize = this.fbFeatures.length;
		
		for (int j = 0; j < this.fbFeatures.length; j++) {
			FBFeatureSet d = this.fbFeatures[j];
			workspace[j] = d.middle.x - d.start.x;
			workspace[j+fbSize] = d.middle.y - d.start.y;
		}
		
		Arrays.sort(workspace, 0, fbSize);
		float dx = (float) median.evaluate(workspace, 0, fbSize);
		Arrays.sort(workspace, fbSize, fbSize+fbSize);
		float dy = (float) median.evaluate(workspace, fbSize, fbSize);
		
		return new Point2dImpl(dx,dy);
	}

}
