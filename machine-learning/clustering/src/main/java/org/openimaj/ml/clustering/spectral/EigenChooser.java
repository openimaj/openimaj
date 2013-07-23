package org.openimaj.ml.clustering.spectral;

import java.util.Iterator;

import org.openimaj.ml.clustering.spectral.FBEigenIterator.Mode;
import org.openimaj.util.pair.DoubleObjectPair;

import ch.akuhn.matrix.Vector;
import ch.akuhn.matrix.eigenvalues.FewEigenvalues;

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

	/**
	 * Make a coarse decision of the number of eigen vectors to extract in the first place
	 * with the knowledge of the eigen values that will likely be important
	 * @param eig the current eigen decompositon
	 * @param direction the direction (backward means smallest to biggest)
	 * @param total the total eigen vectors which will be produced
	 * @return the prepared eigen values
	 */
	public abstract FewEigenvalues prepare(FewEigenvalues eig, Mode direction, int total) ;
}