package org.openimaj.image.processing.face.util;

import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.face.detection.DetectedFace;

/**
 * Given a detected face, draw it to some MBFImage in a sensible way.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 * @param <DETECTED_FACE>
 *            type of {@link DetectedFace}
 */
public interface DetectedFaceRenderer<DETECTED_FACE extends DetectedFace> {

	/**
	 * @param image
	 *            the image to draw onto
	 * @param thickness
	 *            the line thickness
	 * @param f
	 *            the detected face to draw
	 */
	public void drawDetectedFace(MBFImage image, int thickness, DETECTED_FACE f);
}
