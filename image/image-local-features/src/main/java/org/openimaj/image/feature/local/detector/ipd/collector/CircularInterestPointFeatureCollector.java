package org.openimaj.image.feature.local.detector.ipd.collector;

import org.openimaj.feature.OrientedFeatureVector;
import org.openimaj.image.FImage;
import org.openimaj.image.feature.local.detector.ipd.extractor.InterestPointGradientFeatureExtractor;
import org.openimaj.image.feature.local.engine.InterestPointImageExtractorProperties;
import org.openimaj.image.feature.local.interest.AbstractIPD.InterestPointData;
import org.openimaj.image.feature.local.keypoints.InterestPointKeypoint;

public class CircularInterestPointFeatureCollector extends InterestPointFeatureCollector{
	private InterestPointGradientFeatureExtractor extractor;

	public CircularInterestPointFeatureCollector(InterestPointGradientFeatureExtractor extractor){
		this.extractor = extractor;
	}
	
	@Override
	public void foundInterestPoint(FImage image,InterestPointData point){
		InterestPointImageExtractorProperties<Float,FImage> property = new InterestPointImageExtractorProperties<Float,FImage>(image,point,false);
		OrientedFeatureVector[] extracted = extractor.extractFeature(property);
		
		for(OrientedFeatureVector feature : extracted){
			features.add(new InterestPointKeypoint(feature,point));
		}
		
	}

}
