package org.openimaj.image.feature.local.detector.ipd.finder;

import java.util.TreeSet;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.feature.local.interest.IPDSelectionMode;
import org.openimaj.image.feature.local.interest.InterestPointData;
import org.openimaj.image.feature.local.interest.InterestPointDetector;
import org.openimaj.image.feature.local.keypoints.InterestPointKeypoint;
import org.openimaj.math.geometry.shape.Ellipse;

public class CharacteristicOctaveInterestPointFinder<T extends InterestPointData> extends OctaveInterestPointFinder<T> {

	private static final double DEFAULT_MAX_DISTANCE = 4;
	private static final double DEFAULT_MAX_ROTATION = (Math.PI / 180.0) * 15.0;
	private static final double DEFAULT_MAX_AXIS_RATIO = 0.1;
	public double maxDistance = DEFAULT_MAX_DISTANCE;
	public double maxRotation = DEFAULT_MAX_ROTATION;
	public double maxAxisRatio = DEFAULT_MAX_AXIS_RATIO;

	public CharacteristicOctaveInterestPointFinder(InterestPointDetector<T> detector, IPDSelectionMode selectionMode) {
		super(detector, selectionMode);
	}
	
	@Override
	public void finish() {
		LocalFeatureList<InterestPointKeypoint<T>> locatedFeatures = this.listener.getFeatures();
		TreeSet<Integer> toRemove = new TreeSet<Integer>();
		for (int i = 0; i < locatedFeatures.size(); i++) {
			InterestPointKeypoint<T> kp1 = locatedFeatures.get(i);
			for (int j = i+1; j < locatedFeatures.size(); j++) {
				InterestPointKeypoint<T> kp2 = locatedFeatures.get(j);
				if(similarTo(kp1,kp2)){
					if(kp1.location.score >= kp2.location.score){
						toRemove.add(j);
					}
					else{
						toRemove.add(i);
					}
				}
			}	
		}
		int nRemove = 0;
		for(int index : toRemove){
			locatedFeatures.remove(index - nRemove++);
		}
	}

	private boolean similarTo(InterestPointKeypoint<T> kp1,InterestPointKeypoint<T> kp2) {
		boolean similar = true;
		// Similar position
		similar = Math.sqrt(Math.pow(kp1.x -kp2.x,2) +  Math.pow(kp1.y -kp2.y,2)) < maxDistance ; 
		if(!similar) return false;
		Ellipse e1 = kp1.location.getEllipse();
		Ellipse e2 = kp2.location.getEllipse();
		// Ellipse with a similar rotation
		similar = Math.abs(e1.getRotation() - e2.getRotation()) < maxRotation; 
		if(!similar) return false;
		
		// Similar semi-major and semi-minor axis ratio
		similar = Math.abs((e1.getMinor()/e1.getMajor()) - (e2.getMinor()/e2.getMajor())) < maxAxisRatio ;
		if(!similar) return false;
		
		return true;
	}

}
