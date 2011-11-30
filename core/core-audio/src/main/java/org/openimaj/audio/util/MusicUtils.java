/**
 * 
 */
package org.openimaj.audio.util;

/**
 *	Static utility methods that are related to music (more semantic
 *	than audio utilities).
 *	<p>	
 *	For working with notes, see {@link WesternScaleNote}.
 *
 *	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 27 Nov 2011
 */
public class MusicUtils
{
	/**
	 * 	Given a beats-per-minute rate, returns the number of milliseconds
	 * 	in a single beat.
	 * 
	 *	@param bpm Beats per minute
	 *	@return Number of milliseconds per beat.
	 */
	public static int millisPerBeat( float bpm )
	{
		return (int)(60000f/bpm);
	}
}
