package org.openimaj.ml.clustering.spectral;


import java.util.Iterator;

import org.openimaj.ml.clustering.spectral.FBEigenIterator.Mode;
import org.openimaj.util.pair.DoubleObjectPair;

import ch.akuhn.matrix.Vector;
import ch.akuhn.matrix.eigenvalues.FewEigenvalues;

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
	@Override
	public FewEigenvalues prepare(FewEigenvalues eig, Mode direction, int total) {
		if(direction == Mode.FORWARD){
			return eig.greatest(count);
		}
		else{
			return eig.lowest(count);
		}
	}

}