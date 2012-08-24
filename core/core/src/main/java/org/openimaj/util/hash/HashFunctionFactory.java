package org.openimaj.util.hash;

/**
 * A factory for producing {@link HashFunction}s.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <OBJECT>
 *            type of object being hashed
 */
public interface HashFunctionFactory<OBJECT> {
	/**
	 * Construct a new {@link HashFunction}.
	 * 
	 * @return the new {@link HashFunction}
	 */
	public abstract HashFunction<OBJECT> create();
}
