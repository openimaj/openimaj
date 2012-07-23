package org.openimaj.tools.globalfeature.type;

import org.kohsuke.args4j.Option;
import org.openimaj.feature.FeatureVector;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.face.detection.FaceDetectorFeatures;
import org.openimaj.image.processing.face.detection.SandeepFaceDetector;
import org.openimaj.tools.globalfeature.GlobalFeatureExtractor;

/**
 * Sandeep faces
 * @see SandeepFaceDetector
 */
public class ColourFacesExtractor extends GlobalFeatureExtractor {
	@Option(name="--face-feature", aliases="-ff", required=true, usage="type of face feature to extract")
	FaceDetectorFeatures mode;

	@Override
	public FeatureVector extract(MBFImage image, FImage mask) {
		if (mask != null)
			System.err.println("Warning: COLOR_FACES doesn't support masking");

		SandeepFaceDetector fd = new SandeepFaceDetector();
		return mode.getFeatureVector(fd.detectFaces(image), image);
	}
}
