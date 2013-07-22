package org.openimaj.ml.clustering.spectral;

import gov.sandia.cognition.math.matrix.Vector;

import java.util.Iterator;

import org.openimaj.util.pair.DoubleObjectPair;

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
	public int nEigenVectors(Iterator<DoubleObjectPair<Vector>> vals, int total) {
		return count;
	}
	
}