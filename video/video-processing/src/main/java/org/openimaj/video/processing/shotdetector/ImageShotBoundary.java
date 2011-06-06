/**
 * 
 */
package org.openimaj.video.processing.shotdetector;

import org.openimaj.image.Image;
import org.openimaj.video.timecode.VideoTimecode;

/**
 * 	A class that extends the simple shot boundary information by storing
 * 	a keyframe that represents the shot boundary.
 * 
 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 1 Jun 2011
 */
public class ImageShotBoundary<T extends Image<?,T>> extends ShotBoundary
{
	/** An image at the shot boundary */
	public T imageAtBoundary = null;
	
	/**
	 * 	Constructor that allows construction of an image-based shot
	 * 	boundary.
	 * 
	 *  @param timecode The timecode of the shot boundary
	 *  @param img The image at the shot boundary
	 */
	public ImageShotBoundary( VideoTimecode timecode, T img )
    {
	    super( timecode );
    }
}
