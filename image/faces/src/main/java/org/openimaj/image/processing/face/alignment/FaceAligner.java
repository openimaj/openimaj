package org.openimaj.image.processing.face.alignment;

import org.openimaj.image.FImage;
import org.openimaj.image.processing.face.parts.DetectedFace;

/**
 * A FaceAligner produces aligned face patches 
 * (i.e. with the eyes aligned or more). These
 * can be used for building features for 
 * recognition, etc.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public interface FaceAligner {
	/**
	 * For the provided face detection, return an
	 * aligned version of the face.
	 * 
	 * @param face the face to align
	 * @return aligned face
	 */
	public FImage align(DetectedFace face);
	
	/**
	 * Return a mask image for aligned faces. 0 pixels
	 * mark background pixels that should be ignored in 
	 * further processing of aligned faces produced by 
	 * this aligner.
	 * 
	 * @return a mask image, or null if masking isn't required
	 */
	public FImage getMask();
}
