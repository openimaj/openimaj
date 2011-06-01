/**
 * 
 */
package org.openimaj.video.processor;

import org.openimaj.image.Image;
import org.openimaj.video.Video;

/**
 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 1 Jun 2011
 */
public abstract class VideoProcessor<X extends Video<T>, T extends Image<?,T>>
{
	/**
	 * 	Process a frame in this video. The processor must determine itself
	 * 	what is to be done with the frame that is processed. It is suggest
	 * 	that subclass processors add listeners for processed frames if they
	 * 	are required.
	 * 
	 *  @param frame The frame to process.
	 */
	public abstract void processFrame( T frame );
	
	/**
	 * 	Process the given video using this processor.
	 *  @param video The video to process.
	 */
	public void process( X video )
	{
		T frame = null;
		while( (frame = video.getNextFrame()) != null )
			processFrame( frame );
	}
}
