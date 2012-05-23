package org.openimaj.demos.sandbox.tld;

import org.openimaj.video.tracking.klt.Feature;

/**
 * A forward-backward feature. This is a feature which holds 
 * how well a given feature tracks both in a forward backward sense and
 * in a local neighbourhood normalised cross correlation sense.
 * @author ss
 *
 */
public class FBFeature extends Feature{
	/**
	 * initialise an FBFeature from a normal feature
	 * @param feat
	 */
	public FBFeature(Feature feat) {
		this.x = feat.x;
		this.y = feat.y;
		this.val = feat.val;
	}
	
	/**
	 * @see Feature#Feature()
	 */
	public FBFeature() {
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