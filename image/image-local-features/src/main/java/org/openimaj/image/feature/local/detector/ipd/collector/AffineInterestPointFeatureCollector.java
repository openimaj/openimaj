package org.openimaj.image.feature.local.detector.ipd.collector;

import org.openimaj.feature.OrientedFeatureVector;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.image.FImage;
import org.openimaj.image.feature.local.detector.ipd.extractor.InterestPointGradientFeatureExtractor;
import org.openimaj.image.feature.local.engine.InterestPointImageExtractorProperties;
import org.openimaj.image.feature.local.interest.AbstractIPD.InterestPointData;
import org.openimaj.image.feature.local.keypoints.InterestPointKeypoint;

public class AffineInterestPointFeatureCollector extends InterestPointFeatureCollector{
	private InterestPointGradientFeatureExtractor extractor;

	public AffineInterestPointFeatureCollector(InterestPointGradientFeatureExtractor extractor){
		this.extractor = extractor;
	}
	
	@Override
	public void foundInterestPoint(FImage image,InterestPointData point){
		InterestPointImageExtractorProperties<Float,FImage> property = new InterestPointImageExtractorProperties<Float,FImage>(image,point);
		OrientedFeatureVector[] extracted = extractor.extractFeature(property);
		
		for(OrientedFeatureVector feature : extracted){
			features.add(new InterestPointKeypoint(feature,point));
		}
		
	}
}
