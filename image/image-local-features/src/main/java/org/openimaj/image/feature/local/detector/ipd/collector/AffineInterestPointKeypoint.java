package org.openimaj.image.feature.local.detector.ipd.collector;

import org.openimaj.feature.OrientedFeatureVector;
import org.openimaj.image.feature.local.interest.EllipticInterestPointData;
import org.openimaj.image.feature.local.keypoints.InterestPointKeypoint;

public class AffineInterestPointKeypoint extends InterestPointKeypoint<EllipticInterestPointData>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AffineInterestPointKeypoint(OrientedFeatureVector feature,EllipticInterestPointData point) {
		super(feature,point);
	}

	@Override
	public EllipticInterestPointData createEmptyLocation() {
		return new EllipticInterestPointData();
	}
	
}