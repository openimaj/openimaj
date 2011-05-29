package org.openimaj.image.feature.local.detector.ipd.collector;

import org.openimaj.feature.OrientedFeatureVector;
import org.openimaj.image.FImage;
import org.openimaj.image.feature.local.detector.ipd.extractor.InterestPointGradientFeatureExtractor;
import org.openimaj.image.feature.local.engine.InterestPointImageExtractorProperties;
import org.openimaj.image.feature.local.interest.AbstractIPD.InterestPointData;
import org.openimaj.image.feature.local.keypoints.InterestPointKeypoint;

/**
 * Ignore the local shape of interest points, instead extracting a patch dependant on scale.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class CircularInterestPointFeatureCollector extends InterestPointFeatureCollector{
	
	/**
	 * @param extractor
	 */
	public CircularInterestPointFeatureCollector(InterestPointGradientFeatureExtractor extractor) {
		super(extractor);
	}

	@Override
	public void foundInterestPoint(FImage image,InterestPointData point){
		InterestPointImageExtractorProperties<Float,FImage> property = new InterestPointImageExtractorProperties<Float,FImage>(image,point,false);
		OrientedFeatureVector[] extracted = extractor.extractFeature(property);
		
		for(OrientedFeatureVector feature : extracted){
			features.add(new InterestPointKeypoint(feature,point));
		}
		
	}

	@Override
	public void foundInterestPoint(FImage image, InterestPointData point,double octaveSize) {
		InterestPointImageExtractorProperties<Float,FImage> property = new InterestPointImageExtractorProperties<Float,FImage>(image,point,false);
		OrientedFeatureVector[] extracted = extractor.extractFeature(property);
		
		for(OrientedFeatureVector feature : extracted){
			point = point.clone();
			point.scale *= octaveSize;
			point.x *= octaveSize;
			point.y *= octaveSize;
			features.add(new InterestPointKeypoint(feature,point));
		}
		
	}

}
