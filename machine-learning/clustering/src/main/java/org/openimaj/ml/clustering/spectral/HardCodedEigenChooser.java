package org.openimaj.ml.clustering.spectral;

import gov.sandia.cognition.math.ComplexNumber;

/**
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class HardCodedEigenChooser extends EigenChooser{

	int count;
	/**
	 * @param eigK the number of eigen vectors to select
	 */
	public HardCodedEigenChooser(int eigK) {
		this.count = eigK;
	}
	@Override
	public int nEigenVectors(ComplexNumber[] vals) {
		return count;
	}
	
	
}