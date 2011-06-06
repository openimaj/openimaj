/**
 * 
 */
package org.openimaj.video.processing.shotdetector;

import org.openimaj.video.timecode.VideoTimecode;

/**
 * 	A class for encapsulating shot boundary information.
 * 
 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 1 Jun 2011
 */
public class ShotBoundary
{	
	/** The timecode of the shot boundary */
	public VideoTimecode timecode = null;

	/**
	 * 	Construct a shot boundary using the given video timecode.
	 * 
	 *  @param timecode The timecode
	 */
	public ShotBoundary( VideoTimecode timecode )
	{
		this.timecode = timecode;
	}
	
	/**
	 *  @inheritDoc
	 *  @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
	    return this.timecode.toString();
	}
}
