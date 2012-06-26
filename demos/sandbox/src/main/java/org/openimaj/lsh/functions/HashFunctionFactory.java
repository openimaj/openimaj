package org.openimaj.lsh.functions;

import org.openimaj.feature.DoubleFVComparison;

import cern.jet.random.engine.MersenneTwister;

/**
 * A factory for producing {@link HashFunction}s.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <T> type of {@link HashFunction} produced.
 */
public abstract class HashFunctionFactory<T extends HashFunction<?, T>> {
	public boolean norm = true;
	
	/**
	 * Construct a new {@link HashFunction} using the given
	 * random generator.
	 * @param ndims number of dimensions of data
	 * @param rng the random number generator
	 * @return the new {@link HashFunction}
	 */
	public abstract T create(int ndims, MersenneTwister rng);
	
	/**
	 * Compute a suitable normalisation factor to use in
	 * the {@link HashFunction}s {@link HashFunction#computeHashCode(Object, double)} 
	 * method.
	 * 
	 * @param vector the vector
	 * @return the normalisation factor
	 */
	public double computeNorm(double [] vector) {
		return 0;
	}
	
	public abstract DoubleFVComparison defaultDistanceFunction();
}
