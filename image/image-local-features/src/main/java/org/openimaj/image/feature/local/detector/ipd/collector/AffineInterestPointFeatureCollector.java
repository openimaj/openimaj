package org.openimaj.image.feature.local.detector.ipd.collector;

import org.openimaj.feature.OrientedFeatureVector;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.image.FImage;
import org.openimaj.image.feature.local.detector.ipd.extractor.InterestPointGradientFeatureExtractor;
import org.openimaj.image.feature.local.engine.InterestPointImageExtractorProperties;
import org.openimaj.image.feature.local.interest.AbstractIPD.InterestPointData;
import org.openimaj.image.feature.local.keypoints.InterestPointKeypoint;
import org.openimaj.image.processing.pyramid.gaussian.GaussianOctave;

/**
 * Use the interest point's local shape to extract features from an affine corrected patch at the interest point.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class AffineInterestPointFeatureCollector extends InterestPointFeatureCollector{
	
	/**
	 * @param extractor
	 */
	public AffineInterestPointFeatureCollector(InterestPointGradientFeatureExtractor extractor) {
		super(extractor);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void foundInterestPoint(FImage image,InterestPointData point){
		InterestPointImageExtractorProperties<Float,FImage> property = new InterestPointImageExtractorProperties<Float,FImage>(image,point);
		OrientedFeatureVector[] extracted = extractor.extractFeature(property);
		
		for(OrientedFeatureVector feature : extracted){
			features.add(new InterestPointKeypoint(feature,point));
		}
		
	}

	@Override
	public void foundInterestPoint(FImage image, InterestPointData point, double octaveSize) {
		InterestPointImageExtractorProperties<Float,FImage> property = new InterestPointImageExtractorProperties<Float,FImage>(image,point);
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
