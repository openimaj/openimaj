/**
 * 
 */
package org.openimaj.video.processing.shotdetector;

import org.openimaj.video.timecode.VideoTimecode;

/**
 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 1 Jun 2011
 */
public class ShotBoundary
{	
	public VideoTimecode frameNumber = null;
	
	public ShotBoundary( VideoTimecode frameNumber )
	{
		this.frameNumber = frameNumber;
	}
}
