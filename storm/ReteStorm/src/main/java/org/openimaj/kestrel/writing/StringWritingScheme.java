package org.openimaj.kestrel.writing;

import java.io.UnsupportedEncodingException;
import java.util.List;

import backtype.storm.scheme.StringScheme;

/**
 * Write strings to a single byte[]
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class StringWritingScheme extends StringScheme implements WritingScheme {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8653774925352281662L;

	@Override
	public byte[] serialize(List<Object> objects) {
		try {
			return objects.get(0).toString().getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
}
