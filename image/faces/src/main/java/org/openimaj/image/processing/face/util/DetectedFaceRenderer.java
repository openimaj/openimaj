package org.openimaj.image.processing.face.util;

import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.renderer.ImageRenderer;

/**
 * Given a detected face, draw it to some MBFImage in a sensible way.
 * Currently this is an interface that is not an {@link ImageRenderer}.
 * Perhaps in the future this will be an abstract class which extends {@link ImageRenderer}
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <DETECTED_FACE>
 */
public interface DetectedFaceRenderer<DETECTED_FACE extends DetectedFace> {


	/**
	 * @param image the image to draw onto
	 * @param f the detected face to draw
	 */
	public void drawDetectedFace(MBFImage image, int thickness, DETECTED_FACE f);
}
