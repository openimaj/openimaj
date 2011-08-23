package org.openimaj.image.feature.local.detector.ipd.collector;

import org.openimaj.feature.OrientedFeatureVector;
import org.openimaj.image.feature.local.interest.InterestPointData;
import org.openimaj.image.feature.local.keypoints.InterestPointKeypoint;

public class CircularInterestPointKeypoint extends InterestPointKeypoint<InterestPointData>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public CircularInterestPointKeypoint(int length){
		super(length);
	}
	
	public CircularInterestPointKeypoint(OrientedFeatureVector feature,InterestPointData point) {
		super(feature,point);
	}

	@Override
	public InterestPointData createEmptyLocation() {
		return new InterestPointData();
	}
	
}