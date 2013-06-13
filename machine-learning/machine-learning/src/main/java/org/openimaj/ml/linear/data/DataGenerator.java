package org.openimaj.ml.linear.data;

import gov.sandia.cognition.math.matrix.Matrix;

import org.openimaj.util.pair.Pair;

/**
 * Generates instances of some system of the form:
 * 
 * <code>Y = f(X)</code>
 * 
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @param <T>
 *            Matrix type
 * 
 */
public interface DataGenerator<T extends Matrix> {
	/**
	 * @return The X and Y of this generator's Y = F(X)
	 */
	public Pair<T> generate();
}
