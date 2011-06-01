/**
 * 
 */
package org.openimaj.video.processing.shotdetector;

import org.openimaj.image.Image;
import org.openimaj.video.timecode.VideoTimecode;

/**
 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 1 Jun 2011
 */
public class ImageShotBoundary<T extends Image<?,T>> extends ShotBoundary
{
	public T imageAtBoundary = null;
	
	public ImageShotBoundary( VideoTimecode frameNumber, T img )
    {
	    super( frameNumber );
    }

}
