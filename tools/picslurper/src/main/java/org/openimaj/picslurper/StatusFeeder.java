package org.openimaj.picslurper;

import java.io.IOException;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public interface StatusFeeder {
	/**
	 * Start feeding statuses to the provided picslurper. This method should not block
	 * @param slurper
	 * @throws IOException 
	 */
	public void feedStatus(PicSlurper slurper) throws IOException;
}
