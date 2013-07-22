package org.openimaj.ml.clustering.spectral;

import gov.sandia.cognition.math.matrix.Vector;

import java.util.Iterator;

import org.openimaj.util.pair.DoubleObjectPair;

/**
 * Method which makes a decision on how many eigen vectors to select
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public abstract class EigenChooser{
	/**
	 * @param vals 
	 * @param totalEigenVectors the total number of eigen vectors
	 * @return count the eigen vectors
	 */
	public abstract int nEigenVectors(Iterator<DoubleObjectPair<Vector>> vals, int totalEigenVectors);
}