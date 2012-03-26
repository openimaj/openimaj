package org.openimaj.experiment.dataset;

/**
 * An {@link Identifiable} is an object that has an associated identifier.
 * Two instances of {@link Identifiable} with the same identifier
 * should be considered equal. 
 * 
 * {@link Identifiable}s are used in {@link Dataset} as a way
 * of relating a Java object instance back to the original
 * data entity from which it was derived. Within a {@link Dataset}
 * each unique instance should have a unique identifier. 
 * {@link Dataset}s can of course contain multiple instances of the
 * same {@link Identifiable}.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 */
public interface Identifiable {
	/**
	 * @return the identifier of this object
	 */
	public String getID();
	
	/**
	 * Tests if this {@link Identifiable} is equal to a given  
	 * {@link Object} instance. Equality is defined by equality 
	 * of the identifier.
	 * 
	 * @param o the object to compare to
	 * @return true if equal; false otherwise
	 */
	public boolean equals(Object o);
}
