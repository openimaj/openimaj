package org.openimaj.image.processing.face.alignment;

import org.openimaj.image.FImage;
import org.openimaj.image.processing.face.detection.DetectedFace;

/**
 * A FaceAligner that does nothing, and just passes on the
 * patch from the DetectedFace. Useful where you are benchmarking
 * from a set where the faces are already aligned. 
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class NullAligner implements FaceAligner<DetectedFace> {

	@Override
	public FImage align(DetectedFace face) {
		return face.getFacePatch();
	}

	@Override
	public FImage getMask() {
		return null;
	}
}
