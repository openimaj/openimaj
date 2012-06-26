package org.openimaj.demos.sandbox.tld;

import org.openimaj.video.tracking.klt.Feature;

/**
 * A forward-backward feature. This is a holds 
 * how well a given feature tracks both in a forward backward sense and
 * in a local neighbourhood normalised cross correlation sense. Also
 * the 3 features which were used to calculate these values are held
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class FBFeatureSet{
	
	/**
	 * The feature in the "current" image. i.e. the feature which we are tracking from
	 */
	public Feature start;
	/**
	 * The feature in the "next" image. i.e. the feature we are tracking to.
	 */
	public Feature middle;
	/**
	 * The feature in the "current" image as tracked back from the "next" image
	 */
	public Feature end;
	/**
	 * initialise an FBFeature from a normal feature
	 * @param start @see {@link #start}
	 * @param middle @see {@link #middle} 
	 * @param end @see {@link #end}
	 */
	public FBFeatureSet(Feature start, Feature middle, Feature end) {
		this.start = start;
		this.middle = middle;
		this.end = end;
	}
	
	/**
	 * @see Feature#Feature()
	 */
	public FBFeatureSet() {
		super();
	}
	/**
	 * The forward-backward distance. 
	 * This is the euclidian distance of this feature as tracked from the current image to the next and back to the current.
	 * Lower numbers imply a better feature
	 */
	public float forwardBackDistance;
	/**
	 * How well the local neighbourhood of the feature correlates between the current and next image
	 */
	public float normalisedCrossCorrelation;
}