package org.openimaj.demos.sandbox.tld;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.math.stat.descriptive.rank.Median;
import org.openimaj.image.FImage;
import org.openimaj.image.analysis.algorithm.TemplateMatcher;
import org.openimaj.image.analysis.algorithm.TemplateMatcher.TemplateMatcherMode;
import org.openimaj.math.geometry.line.Line2d;
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
 * @author ss
 *
 */
public class TLDFrontBackMedianFlowTracker {
	
	private static final int WINDOW_SIZE = 10;
	private static final int HALF_WINDOW_SIZE = WINDOW_SIZE/2;
	private static PatchMatcher normalisedCrossCorrelation = new PatchMatcher.NORM_CORRELATION_COEFFICIENT();
	private List<FBFeature> fbFeatures;
	/**
	 * The features are tracked as follows:
	 * 
	 * CurrentImage -> NextImage -> CurrentImage
	 * 
	 * 2 scores are calculated for each successfully tracked feature:
	 * {@link FBFeature#forwardBackDistance} == the euclidian distance between the original feature and the feature tracked from the next image to the current image
	 * {@link FBFeature#normalisedCrossCorrelation} == the normalised cross correlation between the feature in this image tracked to the next image
	 * 
	 * @param current The current frame, current features originate here 
	 * @param next The next frame, current features are tracked to, and then back from points in this image
	 * @param currentFeatures
	 * @param currentToNextFeatures
	 * @param nextToCurrentFeatures
	 * @param currentBoundingBox
	 */
	public TLDFrontBackMedianFlowTracker(FImage current, FImage next, FeatureList currentFeatures,FeatureList currentToNextFeatures,FeatureList nextToCurrentFeatures, Rectangle currentBoundingBox) {
		List<FBFeature> fbFeatures = new ArrayList<FBFeature>();
		Iterator<Feature> currentFeaturesItr = currentFeatures.iterator();
		Iterator<Feature> currentToNextFeaturesItr = currentToNextFeatures.iterator();
		
		
		
		// the maximum number of fbs and nccs that will be added.
		double[] fbs = new double[nextToCurrentFeatures.features.length];
		double[] nccs = new double[nextToCurrentFeatures.features.length];
		int valueIndex = 0;
		
		for (Feature featEnd : nextToCurrentFeatures) {
			Feature featStart = currentFeaturesItr.next();
			Feature featMiddle = currentToNextFeaturesItr.next();
			if(featEnd.val < 0 ){
				// this feature didn't survive going from A to B and back to A, it must be unstable, throw it away
				break;
			}
			FBFeature newFeature = new FBFeature(featEnd);
			// Correlation in a window centered around the feature(x,y)
			int featStartX = (int) (featStart.x - HALF_WINDOW_SIZE); featStartX = featStartX < 0 ? 0 : featStartX;
			int featStartY = (int) (featStart.y - HALF_WINDOW_SIZE); featStartY = featStartY < 0 ? 0 : featStartY;
			int featMiddleX = (int) (featMiddle.x - HALF_WINDOW_SIZE); featMiddleX = featMiddleX < 0 ? 0 : featMiddleX;
			int featMiddleY = (int) (featMiddle.y - HALF_WINDOW_SIZE); featMiddleY = featMiddleY < 0 ? 0 : featMiddleY;
			
			
			newFeature.forwardBackDistance = (float) Line2d.distance(featStart.x, featStart.y, featEnd.x, featEnd.y);
			newFeature.normalisedCrossCorrelation = normalisedCrossCorrelation.computeMatchScore(current, featStartX, featStartY, next, featMiddleX, featMiddleY, WINDOW_SIZE, WINDOW_SIZE);
			
			fbs[valueIndex] = newFeature.forwardBackDistance;
			nccs[valueIndex] = newFeature.normalisedCrossCorrelation;
			
			valueIndex ++;
			
			fbFeatures.add(newFeature);
		}
		
		Arrays.sort(fbs, 0, valueIndex);
		Arrays.sort(nccs, 0, valueIndex);
		
		Median median = new Median();
		
		double fbMedian = median.evaluate(fbs, 0, valueIndex);
		double nccMedian = median.evaluate(nccs, 0, valueIndex);
		
		for (Iterator<FBFeature> iterator = fbFeatures.iterator(); iterator.hasNext();) {
			FBFeature fbFeature = iterator.next();
			if(fbFeature.forwardBackDistance > fbMedian || fbFeature.normalisedCrossCorrelation < nccMedian) iterator.remove();
		}
		
		this.fbFeatures = fbFeatures;
	}

}
