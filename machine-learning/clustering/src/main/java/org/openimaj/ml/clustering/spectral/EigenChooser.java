package org.openimaj.ml.clustering.spectral;

import gov.sandia.cognition.math.ComplexNumber;

/**
 * Method which makes a decision on how many eigen vectors to select
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public abstract class EigenChooser{
	/**
	 * @param vals 
	 * @return count the eigen vectors
	 */
	public abstract int nEigenVectors(ComplexNumber[] vals);
}