package org.openimaj.image.feature.local.interest;

import java.util.List;

import org.openimaj.image.feature.local.interest.AbstractStructureTensorIPD.InterestPointData;

public interface IPDSelectionMode {
	public List<InterestPointData> selectPoints(InterestPointDetector detector);
	public class Count implements IPDSelectionMode{
		private int count;
		public Count(int count){
			this.count = count;
		}
		@Override
		public List<InterestPointData> selectPoints(InterestPointDetector detector) {
			return detector.getInterestPoints(count);
		}	
	}
	class All implements IPDSelectionMode{

		@Override
		public List<InterestPointData> selectPoints(InterestPointDetector detector) {
			return detector.getInterestPoints();
		}
		
	}

	class Threshold implements IPDSelectionMode{

		private float threshold;
		public Threshold(float threshold){
			this.threshold = threshold;
		}
		@Override
		public List<InterestPointData> selectPoints(InterestPointDetector detector) {
			return detector.getInterestPoints(threshold);
		}	
	}
}



