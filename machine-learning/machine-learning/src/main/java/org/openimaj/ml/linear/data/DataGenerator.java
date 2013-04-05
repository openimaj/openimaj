package org.openimaj.ml.linear.data;

import gov.sandia.cognition.math.matrix.Matrix;

import org.openimaj.util.pair.Pair;


/**
 * Generates instances of some system of the form:
 * 
 * Y = f(X)
 * 
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public interface DataGenerator<T extends Matrix> {
	/**
	 * @return The X and Y of this generator's Y = F(X)
	 */
	public Pair<T> generate();
}
