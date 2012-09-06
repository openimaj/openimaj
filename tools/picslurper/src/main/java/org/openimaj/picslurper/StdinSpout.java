package org.openimaj.picslurper;

import java.io.InputStream;

/**
 * A {@link LocalTweetSpout} fed from the stdin
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
@SuppressWarnings("serial")
public class StdinSpout extends LocalTweetSpout {

	private boolean usedSTDIN;

	@Override
	protected InputStream nextInputStream() throws Exception {
		if(usedSTDIN) return null;
		usedSTDIN = true;
		return System.in;
	}

}
