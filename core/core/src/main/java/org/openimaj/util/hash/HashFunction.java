package org.openimaj.util.hash;

/**
 * Interface describing a hash function: an object that can compute a hashcode
 * for another object.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <OBJECT>
 *            Object being hashed
 */
public interface HashFunction<OBJECT> {
	/**
	 * Compute the hash code for the object
	 * 
	 * @param object
	 *            the object
	 * 
	 * @return the hash code
	 */
	public abstract int computeHashCode(OBJECT object);
}
