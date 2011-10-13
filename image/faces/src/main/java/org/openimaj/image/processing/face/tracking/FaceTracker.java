package org.openimaj.image.processing.face.tracking;

import java.util.List;

import org.openimaj.image.Image;
import org.openimaj.image.processing.face.detection.DetectedFace;

/**
 * 	An interface for classes that are able to track faces within images. 
 * 
 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 13 Oct 2011
 *
 *  @param <I> The type of image on which the face tracker will operate
 */
public interface FaceTracker<I extends Image<?,I>>
{
	/**
	 * 	Given the image, can a face be tracked on the image. The implementation
	 * 	must deal with finding the face in the first place, if this is the first
	 * 	frame it's seen, and it must deal with storage of state as the tracking
	 * 	takes place.
	 * 
	 *  @param img The image to track a face in
	 *  @return A list of detected/tracked faces which could be empty
	 *  	if no faces are found in the image. 
	 */
	public List<DetectedFace> trackFace( I img );
}
