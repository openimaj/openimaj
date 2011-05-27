package org.openimaj.image.feature.local.detector.ipd.collector;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.image.FImage;
import org.openimaj.image.feature.local.interest.AbstractIPD.InterestPointData;
import org.openimaj.image.feature.local.keypoints.InterestPointKeypoint;

public abstract class InterestPointFeatureCollector {
	
	protected MemoryLocalFeatureList<InterestPointKeypoint> features;

	public InterestPointFeatureCollector(){
		this.features = new MemoryLocalFeatureList<InterestPointKeypoint>();
	}
	
	public LocalFeatureList<InterestPointKeypoint> getFeatures() {
		return this.features;
	}
	
	public abstract void foundInterestPoint(FImage image,InterestPointData point);
}
