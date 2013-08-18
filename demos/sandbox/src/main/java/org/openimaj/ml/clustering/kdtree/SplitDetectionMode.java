package org.openimaj.ml.clustering.kdtree;

import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.openimaj.util.array.ArrayUtils;

import scala.actors.threadpool.Arrays;

/**
 * Given a vector, tell me the split
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public interface SplitDetectionMode{
	/**
	 * Splits clusters becuase they don't have exactly the same value!
	 */
	public static class MEDIAN implements SplitDetectionMode{
		@Override
		public double detect(double[] col) {
			double mid = ArrayUtils.quickSelect(col, col.length/2);
			if(ArrayUtils.minValue(col) == mid) 
				mid += Double.MIN_NORMAL;
			if(ArrayUtils.maxValue(col) == mid) 
				mid -= Double.MIN_NORMAL;
			return 0;
		}
	}
	
	/**
	 * Use the mean to split
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 */
	public static class MEAN implements SplitDetectionMode{

		@Override
		public double detect(double[] vec) {
			return new Mean().evaluate(vec);
		}
		
	}
	
	/**
	 * Find the median, attempt to find a value which keeps clusters together
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 */
	public static class VARIABLE_MEDIAN implements SplitDetectionMode{

		private double tolchange;
		/**
		 * Sets the change tolerance to 0.1 (i.e. if the next value is different by more than value * 0.1, we switch)
		 */
		public VARIABLE_MEDIAN() {
			this.tolchange = 0.0001;
		}
		
		/**
		 * @param tol if the next value is different by more than value * tol we found a border
		 */
		public VARIABLE_MEDIAN(double tol) {
			this.tolchange = tol;
		}
		
		@Override
		public double detect(double[] vec) {
			Arrays.sort(vec);
			// Find the median index
			int medInd = vec.length/2;
			double medVal = vec[medInd];
			if(vec.length % 2 == 0){
				medVal += vec[medInd+1];
				medVal /= 2.;
			}
			
			
			boolean maxWithinTol = withinTol(medVal,vec[vec.length-1]);
			boolean minWithinTol = withinTol(medVal,vec[0]);
			if(maxWithinTol && minWithinTol) 
			{
				// degenerate case, the min and max are not beyond the tolerance, return the median
				return medVal;
			}
			// The split works like:
			// < val go left
			// >= val go right
			if(maxWithinTol){
				// search left
				for (int i = medInd; i > 0; i--) {
					if(!withinTol(vec[i],vec[i-1])){
						return vec[i];
					}
				}
			}
			else{
				// search right
				for (int i = medInd; i < vec.length-1; i++) {
					if(!withinTol(vec[i],vec[i+1])){
						return vec[i+1];
					}
				}
			}
			
			
			
			return 0;
		}

		private boolean withinTol(double a, double d) {
			return Math.abs(a - d) / Math.abs(a) < this.tolchange;
		}
		
	};
	public abstract double detect(double[] vec);
}