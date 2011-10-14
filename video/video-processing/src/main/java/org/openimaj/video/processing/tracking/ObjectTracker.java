/**
 * 
 */
package org.openimaj.video.processing.tracking;

import java.util.List;

import org.openimaj.math.geometry.shape.Rectangle;

/**
 *	An interface for classes that can track objects in video (or image streams).
 *
 *	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 14 Oct 2011
 *
 *	@param <O> The type of objects that will be tracked 
 *	@param <I> The type of image in which the objects will be tracked
 */
public interface ObjectTracker<O,I>
{
	/**
	 *	Initialise the tracking with the given target area in the given image.
	 *  
	 *	@param bounds The target area to track in the image
	 *	@param image The image to initialise the tracking with
	 *	@return A list of found objects (may be empty if no object found);
	 *		should not be null.
	 */
	public List<O> initialiseTracking( Rectangle bounds, I image );

	/**
	 * 	Track the object in the image.
	 *  
	 *	@param image The image to continue tracking the object within.
	 *	@return The list of objects found in the image
	 */
	public List<O> trackObject( I image );
	
}
