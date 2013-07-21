package org.openimaj.ml.clustering.spectral;

import gov.sandia.cognition.math.ComplexNumber;

/**
 * Attempts to automatically choose the number of eigen vectors based on the 
 * relative gap between eigen values. In spectral clustering the gap between the 
 * eigen values of "good" clusters jumps. This class ignores the gap between 0 and 
 * the next item because 0s represent completely isolated objects and in all but the trivial
 * case we must stop after we have run out of 0s.
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class AutoSelectingEigenChooser extends EigenChooser {

	private double relativeGap;
	private double maxSelect;

	/**
	 * @param relativeGap the gap between previous and current (treated as absolute if previous value == 0)
	 * @param maxSelect
	 */
	public AutoSelectingEigenChooser(double relativeGap, double maxSelect) {
		this.relativeGap = relativeGap;
		this.maxSelect = maxSelect;
	}

	@Override
	public int nEigenVectors(ComplexNumber[] vals) {
		int count = 0;
		double prev = vals[vals.length-1].getMagnitude();
		for (int i = vals.length-2; i >= 0; i--) {
			double val = vals[i].getMagnitude();
			if(prev != 0){
				double d = val - prev;
				double l = prev * relativeGap;
				if(d > l) {
					count++;
					break;
				}
			}
			prev = val;
			count ++;
		}
		int maxCount = (int) (vals.length * maxSelect);
		if(count > maxCount){
			return maxCount;
		}
		return count;
	}


}
