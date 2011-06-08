/**
 * 
 */
package org.openimaj.video.timecode;

import org.openimaj.time.Timecode;

/**
 * 	A class for storing video timecodes.
 * 
 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 1 Jun 2011
 */
public abstract class VideoTimecode 
	implements Comparable<VideoTimecode>, Timecode
{
	/**
	 * 	Returns the frame number of the frame represented by the
	 * 	video timecode. This is necessary to allow comparison between
	 * 	video timecodes (even of different types).
	 * 
	 *	@return The frame number of the frame represented by this timecode.
	 */
	public abstract int getFrameNumber();
}
