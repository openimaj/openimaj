package org.openimaj.math.hash;

import java.util.Date;

import org.openimaj.util.hash.HashFunctionFactory;

import cern.jet.random.engine.MersenneTwister;

/**
 * A {@link HashFunctionFactory} for producing {@link StringMurmurHashFunction}s
 * with randomly assigned seeds.
 * 
 * @see StringMurmurHashFunction#StringMurmurHashFunction(int)
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class StringMurmurHashFunctionFactory implements HashFunctionFactory<String> {
	private MersenneTwister rng;

	/**
	 * Construct the factory with a newly constructed {@link MersenneTwister}
	 * seeded with the current time.
	 */
	public StringMurmurHashFunctionFactory() {
		this.rng = new MersenneTwister(new Date());
	}

	/**
	 * Construct the factory with the given random generator
	 * 
	 * @param rng
	 *            the random generator
	 */
	public StringMurmurHashFunctionFactory(MersenneTwister rng) {
		this.rng = rng;
	}

	@Override
	public StringMurmurHashFunction create() {
		return new StringMurmurHashFunction(rng.nextInt());
	}
}
