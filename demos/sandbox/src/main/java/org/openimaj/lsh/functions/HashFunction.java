package org.openimaj.lsh.functions;

/**
 * A hash function. Basically an object that can compute a hashcode for another
 * object.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <O>
 *            Object being hashed
 */
public interface HashFunction<O> {
	/**
	 * Compute the hash code for the object
	 * 
	 * @param object
	 *            the object
	 * 
	 * @return the hash code
	 */
	public abstract int computeHashCode(O object);
}
