/**
 * 
 */
package org.openimaj.video.processing.shotdetector;

import org.openimaj.image.Image;
import org.openimaj.video.timecode.VideoTimecode;

/**
 * 	Interface for objects that wish to listen to shot detections from
 * 	the shot detector. 
 * 
 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 26 Sep 2011
 */
public interface ShotDetectedListener<T extends Image<?,T>>
{
	/**
	 * 	Called when a shot is detected by the shot detector with the
	 * 	shot boundary definition and an associated key frame.
	 * 
	 *  @param sb The shot boundary definition
	 *  @param vk The keyframe
	 */
	public void shotDetected( ShotBoundary sb, VideoKeyframe<T> vk );
	
	/**
	 * 	Called every time a differential between two frames has been
	 * 	calculated.
	 * 
	 *  @param vt The timecode of the differential frame.
	 *  @param d The differential value
	 *  @param frame The current frame
	 */
	public void differentialCalculated( VideoTimecode vt, double d, T frame );
}
