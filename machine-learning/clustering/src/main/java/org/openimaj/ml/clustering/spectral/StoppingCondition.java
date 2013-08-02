package org.openimaj.ml.clustering.spectral;

import java.util.List;

/**
 * The stopping condition for a multiview spectral clustering algorithm
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public interface StoppingCondition {
	/**
	 * Called once at the beggining of each full iteration
	 * 
	 * @param answers the list of current eigen vectors 
	 * @return true if this iteration should not happen
	 */
	public boolean stop(List<double[][]> answers);
	
	/**
	 * Counts the iterations 
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 */
	public static final class HardCoded implements StoppingCondition{
		int count = 0;
		private int max;
		/**
		 * @param iters
		 */
		public HardCoded(int iters) {
			this.max = iters;
		}
		@Override
		public boolean stop(List<double[][]> answers) {			
			return count ++ < this.max;
		}
		
	}
}

