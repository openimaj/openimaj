package org.openimaj.text.nlp.sentiment.type;

import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class UnrecognisedMapException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3135632880703772301L;

	/**
	 * @param expectedKey
	 */
	public UnrecognisedMapException(List<String> expectedKey) {
		super("Can't load map, expected keys: " + StringUtils.join(expectedKey, ", "));
	}

	/**
	 * @param expectedKey
	 */
	public UnrecognisedMapException(String ... expectedKey) {
		super("Can't load map, expected keys: " + StringUtils.join(expectedKey, ", "));
	}
}
