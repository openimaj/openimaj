package org.openimaj.io;

import java.io.InputStream;

/**
 * Interface for classes capable of reading objects
 * from a {@link InputStream}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <T> Type of object being read.
 */
public interface ObjectReader<T> {
	/**
	 * Read an object from the stream
	 * 
	 * @param stream the stream
	 * @return the object
	 */
	public T read(InputStream stream);
}
