/**
 * 
 */
package org.openimaj.video;

import org.openimaj.image.Image;
import org.openimaj.video.timecode.VideoTimecode;

/**
 *	This is a helper class that is able to wrap an image, its timecode and
 *	other information related to video.
 *
 *	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *  @created 1 Mar 2012
 *	@version $Author$, $Revision$, $Date$
 *
 *	@param <T> The type of the video frame 
 */
public class VideoFrame<T extends Image<?,T>>
{
	/** The timecode of the frame */
	public VideoTimecode timecode = null;
	
	/** The frame of video */
	public T frame = null;
	
	/**
	 * 	Constructor
	 * 	@param frame The frame
	 * 	@param timecode The timecode
	 */
	public VideoFrame( T frame, VideoTimecode timecode )
	{
		this.frame = frame;
		this.timecode = timecode;
	}
}
