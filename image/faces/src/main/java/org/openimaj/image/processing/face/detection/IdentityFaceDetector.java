package org.openimaj.image.processing.face.detection;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.MBFImage;

/**
 * A face detector that does nothing other than wrap the
 * input image in a single {@link DetectedFace} object.
 * <p>
 * This class is only likely to be useful for performing
 * evaluations of techniques that use datasets where a
 * face has already been extracted/cropped into an image.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <IMAGE> The type of image
 */
public class IdentityFaceDetector<IMAGE extends Image<?, IMAGE>> implements FaceDetector<DetectedFace, IMAGE> {

	@Override
	public void readBinary(DataInput in) throws IOException {
		//Do nothing
	}

	@Override
	public byte[] binaryHeader() {
		return IdentityFaceDetector.class.getName().getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		//Do nothing
	}

	@Override
	public List<DetectedFace> detectFaces(IMAGE image) {
		DetectedFace face = null;
		if (image instanceof FImage)
			face = new DetectedFace(image.getBounds(), ((FImage)((Object)image)));
		else if (image instanceof MBFImage)
			face = new DetectedFace(image.getBounds(), ((MBFImage)((Object)image)).flatten());
		else
			throw new RuntimeException("unsupported image type");
		
		List<DetectedFace> faces = new ArrayList<DetectedFace>(1);
		faces.add(face);
		
		return faces;
	}
}
