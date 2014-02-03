package org.openimaj.ml.linear.learner.perceptron;

/**
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public enum PerceptronClass {
	/**
	 * when the sign function returns -1 
	 */
	FALSE {
		@Override
		public int v() {
			// TODO Auto-generated method stub
			return -1;
		}
	},
	/**
	 * when the sign function returns 0
	 */
	NONE {
		@Override
		public int v() {
			return 0;
		}
	},
	/**
	 * 
	 */
	TRUE {
		@Override
		public int v() {
			return 1;
		}
	};
	
	/**
	 * @return the value
	 */
	public abstract int v();
	
	/**
	 * @param d
	 * @return must give a sign or this will break
	 */
	public static PerceptronClass fromSign(double d){
		PerceptronClass[] values = PerceptronClass.values();
		return values[(int) (d+1)];
	}
}
