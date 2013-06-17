package org.openimaj.util.concatenate;

import java.util.List;

/**
 * Interface for objects that can be concatenated together to form a new object.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <IN>
 *            The type that can be concatenated with this
 * @param <OUT>
 *            The resultant type
 */
public interface Concatenatable<IN, OUT> {
	/**
	 * Concatenate all the inputs with this, returning a new object that is the
	 * result of the concatenation.
	 * 
	 * @param ins
	 *            the inputs
	 * @return the concatenated object
	 */
	public OUT concatenate(IN... ins);

	/**
	 * Concatenate all the inputs with this, returning a new object that is the
	 * result of the concatenation.
	 * 
	 * @param ins
	 *            the inputs
	 * @return the concatenated object
	 */
	public OUT concatenate(List<IN> ins);
}
