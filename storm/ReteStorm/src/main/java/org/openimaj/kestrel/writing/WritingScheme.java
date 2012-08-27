package org.openimaj.kestrel.writing;

import java.util.List;

import backtype.storm.spout.Scheme;

/**
 * Given a list of objects defined whose values are the entries of {@link #getOutputFields()},
 * {@link WritingScheme} instances can turn those objects into byte[]
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 * @param <OBJECT> The object type being writting
 *
 */
public interface WritingScheme extends Scheme{
	/**
	 * @param objects
	 * @return serialise the list
	 */
	public byte[] serialize(List<Object> objects);
	
}
