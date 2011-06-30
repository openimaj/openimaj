package org.openimaj.tools.faces.recognition;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import org.openimaj.feature.FloatFVComparison;
import org.openimaj.image.processing.face.alignment.AffineAligner;
import org.openimaj.image.processing.face.feature.FacePatchFeature;
import org.openimaj.image.processing.face.feature.FacialFeatureFactory;
import org.openimaj.image.processing.face.feature.comparison.FaceFVComparator;
import org.openimaj.image.processing.face.feature.comparison.FacialFeatureComparator;
import org.openimaj.image.processing.face.feature.comparison.ReversedLtpDtFeatureComparator;
import org.openimaj.image.processing.face.feature.ltp.ReversedLtpDtFeature;
import org.openimaj.image.processing.face.feature.ltp.TruncatedWeighting;
import org.openimaj.image.processing.face.keypoints.FKEFaceDetector;
import org.openimaj.image.processing.face.keypoints.KEDetectedFace;
import org.openimaj.image.processing.face.recognition.FaceRecognitionEngine;
import org.openimaj.image.processing.face.recognition.SimpleKNNRecogniser;

class FaceRecogniserTrainingToolOptions {
	public enum RecognitionStrategy {
		LTP_DT_TRUNCATED_REVERSED_AFFINE_1NN {
			@Override
			public FaceRecognitionEngine<KEDetectedFace> newRecognitionEngine() {
				FacialFeatureFactory<ReversedLtpDtFeature, KEDetectedFace> factory = new ReversedLtpDtFeature.Factory<KEDetectedFace>(new AffineAligner(), new TruncatedWeighting());
				FacialFeatureComparator<ReversedLtpDtFeature> comparator = new ReversedLtpDtFeatureComparator();

				SimpleKNNRecogniser<ReversedLtpDtFeature, KEDetectedFace> recogniser = new SimpleKNNRecogniser<ReversedLtpDtFeature, KEDetectedFace>(factory, comparator, 1);
				FKEFaceDetector detector = new FKEFaceDetector();
				
				return new FaceRecognitionEngine<KEDetectedFace>(detector, recogniser);
			}
		},
		FACEPATCH_EUCLIDEAN_1NN {
			@Override
			public FaceRecognitionEngine<KEDetectedFace> newRecognitionEngine() {
				FacialFeatureFactory<FacePatchFeature, KEDetectedFace> factory = new FacePatchFeature.Factory();
				FacialFeatureComparator<FacePatchFeature> comparator = new FaceFVComparator<FacePatchFeature>(FloatFVComparison.EUCLIDEAN);

				SimpleKNNRecogniser<FacePatchFeature, KEDetectedFace> recogniser = new SimpleKNNRecogniser<FacePatchFeature, KEDetectedFace>(factory, comparator, 1);
				FKEFaceDetector detector = new FKEFaceDetector();
				
				return new FaceRecognitionEngine<KEDetectedFace>(detector, recogniser);
			}
		}
		;
		
		public abstract FaceRecognitionEngine<?> newRecognitionEngine();
	}
	
	@Option(name="-f", aliases="--file", usage="Recogniser file", required=true)
	File recogniserFile;
	
	@Option(name="-s", aliases="--strategy", usage="Recognition strategy", required=false)
	RecognitionStrategy strategy = RecognitionStrategy.LTP_DT_TRUNCATED_REVERSED_AFFINE_1NN;
	
	@Option(name="-id", aliases="--identifier", usage="Identifier of person", required=false)
	String identifier;
	
	@Argument()
	List<File> files;

	public FaceRecognitionEngine<?> getEngine() throws IOException {
		if (recogniserFile.exists()) {
			return FaceRecognitionEngine.load(recogniserFile);
		}
		
		return strategy.newRecognitionEngine();
	}
}
