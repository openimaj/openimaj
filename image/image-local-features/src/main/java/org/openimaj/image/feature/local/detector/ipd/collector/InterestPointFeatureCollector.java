package org.openimaj.image.feature.local.detector.ipd.collector;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.image.FImage;
import org.openimaj.image.feature.local.detector.ipd.extractor.InterestPointGradientFeatureExtractor;
import org.openimaj.image.feature.local.interest.AbstractIPD.InterestPointData;
import org.openimaj.image.feature.local.keypoints.InterestPointKeypoint;
import org.openimaj.image.processing.pyramid.gaussian.GaussianOctave;

/**
 * An interest point feature collector can be used to hold interest points found in an image.
 * Interest point collectors decide what area of the image is considered "interesting" pass this 
 * information on to their designated extractor.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public abstract class InterestPointFeatureCollector {
	
	protected MemoryLocalFeatureList<InterestPointKeypoint> features;
	protected InterestPointGradientFeatureExtractor extractor;

	/**
	 * Initialise the collector with a memory local feature list types on {@link InterestPointKeypoint}
	 * @param extractor when interest points are found and prepared, inform this extractor
	 */
	public InterestPointFeatureCollector(InterestPointGradientFeatureExtractor extractor){
		this.features = new MemoryLocalFeatureList<InterestPointKeypoint>();
		this.extractor = extractor;
	}
	
	/**
	 * @return the features collected
	 */
	public LocalFeatureList<InterestPointKeypoint> getFeatures() {
		return this.features;
	}
	
	/**
	 * Collect interest points from a single image
	 * @param image
	 * @param point
	 */
	public abstract void foundInterestPoint(FImage image,InterestPointData point);

	/**
	 * Collect interest points from an image known to be in a pyramid with a given octave size
	 * @param image
	 * @param point
	 * @param octaveSize
	 */
	public abstract void foundInterestPoint(FImage image,InterestPointData point, double octaveSize);
}
