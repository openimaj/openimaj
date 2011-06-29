package org.openimaj.image.processing.face.alignment;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

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

	@Override
	public void readBinary(DataInput in) throws IOException {
		// Do nothing
	}

	@Override
	public byte[] binaryHeader() {
		// Do nothing
		return null;
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		// Do nothing
	}
}
