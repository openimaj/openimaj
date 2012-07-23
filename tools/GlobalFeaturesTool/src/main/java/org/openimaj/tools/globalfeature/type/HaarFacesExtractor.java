package org.openimaj.tools.globalfeature.type;

import org.kohsuke.args4j.Option;
import org.openimaj.feature.FeatureVector;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.processing.face.detection.FaceDetectorFeatures;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector.BuiltInCascade;
import org.openimaj.tools.globalfeature.GlobalFeatureExtractor;

/**
 * Haar cascades
 * @see HaarCascadeDetector
 */
public class HaarFacesExtractor extends GlobalFeatureExtractor {
	@Option(name="--face-feature", aliases="-ff", required=true, usage="type of face feature to extract")
	FaceDetectorFeatures mode;

	@Option(name="--cascade", aliases="-c", required=true, usage="the detector cascade to use")
	BuiltInCascade cascade;

	@Override
	public FeatureVector extract(MBFImage image, FImage mask) {
		if (mask != null)
			System.err.println("Warning: HAAR_FACES doesn't support masking");

		HaarCascadeDetector fd = cascade.load();
		return mode.getFeatureVector(fd.detectFaces(Transforms.calculateIntensityNTSC(image)), image);
	}
}
