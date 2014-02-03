package org.openimaj.ml.linear.data;

import org.openimaj.util.pair.IndependentPair;

/**
 *
 * @param <I>
 * @param <D>
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public interface DataGenerator<I, D> {
	
	/**
	 * @return The X and Y of this generator's Y = F(X)
	 */
	public IndependentPair<I,D> generate();
	
}
