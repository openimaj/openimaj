package org.openimaj.util.hash;

/**
 * A factory for producing {@link HashFunction}s.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <HASH>
 *            type of {@link HashFunction} produced.
 * @param <OBJECT>
 *            type of object being hashed
 */
public interface HashFunctionFactory<HASH extends HashFunction<OBJECT>, OBJECT> {
	/**
	 * Construct a new {@link HashFunction}.
	 * 
	 * @return the new {@link HashFunction}
	 */
	public abstract HASH create();
}
