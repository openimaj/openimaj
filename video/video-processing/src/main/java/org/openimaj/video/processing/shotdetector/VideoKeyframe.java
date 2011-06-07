/**
 * 
 */
package org.openimaj.video.processing.shotdetector;

import org.openimaj.image.Image;
import org.openimaj.video.timecode.VideoTimecode;

/**
 * 	A class that represents a keyframe of a video. Encapsualtes the image
 * 	representing the keyframe as well as the timecode at which the keyframe
 * 	occurs in the video.
 * 
 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 1 Jun 2011
 */
public class VideoKeyframe<T extends Image<?,T>>
{
	/** An image at the shot boundary */
	public T imageAtBoundary = null;
	
	/** The timecode of the keyframe */
	private VideoTimecode timecode = null;
	
	/**
	 * 	Constructor that allows construction of an image-based shot
	 * 	boundary.
	 * 
	 *  @param timecode The timecode of the shot boundary
	 *  @param img The image at the shot boundary
	 */
	public VideoKeyframe( VideoTimecode timecode, T img )
	{
	    this.imageAtBoundary = img;
	    this.timecode = timecode;
    }
	
	/**
	 * 	Return the image at the shot boundary.
	 *  @return The image at the shot boundary.
	 */
	public T getImage()
	{
		return imageAtBoundary;
	}
	
	/**
	 * 	Returns the timecode of this keyframe.
	 *	@return The timecode of the keyframe in the video.
	 */
	public VideoTimecode getTimecode()
	{
		return this.timecode;
	}
}
