/**
 * 
 */
package org.openimaj.video;

import java.util.EventListener;

import org.openimaj.image.Image;

/**
 *	This interface is used for objects that wish to be informed of particular
 *	important playback positions within a video playback.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 5 Dec 2012
 *	@version $Author$, $Revision$, $Date$
 */
public interface VideoPositionListener extends EventListener
{
	/**
	 * 	The video is at the start (first frame)
	 *	@param vd The video display from whence the video can be retrieved
	 */
	public void videoAtStart( VideoDisplay<? extends Image<?,?>> vd );
	
	/**
	 * 	The video is at the end (last frame)
	 *	@param vd The video display from whence the video can be retrieved
	 */
	public void videoAtEnd( VideoDisplay<? extends Image<?,?>> vd );
}
