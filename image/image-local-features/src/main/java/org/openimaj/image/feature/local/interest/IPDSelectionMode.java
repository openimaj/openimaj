package org.openimaj.image.feature.local.interest;

import java.util.List;


public interface IPDSelectionMode {
	public <T extends InterestPointData> List<T> selectPoints(InterestPointDetector<T> detector);
	public class Count implements IPDSelectionMode{
		private int count;
		public Count(int count){
			this.count = count;
		}
		@Override
		public<T extends InterestPointData> List<T> selectPoints(InterestPointDetector<T> detector) {
			return detector.getInterestPoints(count);
		}	
	}
	class All implements IPDSelectionMode{

		@Override
		public <T extends InterestPointData> List<T> selectPoints(InterestPointDetector<T> detector) {
			return detector.getInterestPoints();
		}
		
	}

	class Threshold implements IPDSelectionMode{

		private float threshold;
		public Threshold(float threshold){
			this.threshold = threshold;
		}
		@Override
		public <T extends InterestPointData> List<T> selectPoints(InterestPointDetector<T> detector) {
			return detector.getInterestPoints(threshold);
		}	
	}
}



