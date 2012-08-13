package org.openimaj.tools.faces.recognition.options;

import org.kohsuke.args4j.CmdLineOptionsProvider;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ProxyOptionHandler;
import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.feature.FVProviderExtractor;
import org.openimaj.feature.FloatFVComparison;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.keypoints.FKEFaceDetector;
import org.openimaj.image.processing.face.detection.keypoints.KEDetectedFace;
import org.openimaj.image.processing.face.feature.FacePatchFeature;
import org.openimaj.image.processing.face.feature.LocalLBPHistogram;
import org.openimaj.image.processing.face.feature.comparison.FaceFVComparator;
import org.openimaj.image.processing.face.feature.comparison.FacialFeatureComparator;
import org.openimaj.image.processing.face.recognition.AnnotatorFaceRecogniser;
import org.openimaj.image.processing.face.recognition.EigenFaceRecogniser;
import org.openimaj.image.processing.face.recognition.FaceRecognitionEngine;
import org.openimaj.image.processing.face.recognition.FisherFaceRecogniser;
import org.openimaj.ml.annotation.basic.KNNAnnotator;
import org.openimaj.ml.annotation.bayes.NaiveBayesAnnotator;
import org.openimaj.tools.faces.recognition.options.Aligners.AlignerDetectorProvider;
import org.openimaj.tools.faces.recognition.options.Aligners.AnyAligner;

/**
 * Standard recognition strategies
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public enum RecognitionStrategy implements CmdLineOptionsProvider {
	/**
	 * EigenFaces with a KNN classifier
	 */
	EigenFaces_KNN {
		@Override
		public RecognitionEngineProvider getOptions() {
			return new RecognitionEngineProvider() {
				@Option(name = "--num-components", usage = "number of components")
				int numComponents = 10;

				@SuppressWarnings("unused")
				@Option(name = "--aligner", usage = "aligner", required = false, handler = ProxyOptionHandler.class)
				AnyAligner aligner = AnyAligner.Identity;
				AlignerDetectorProvider alignerOp;

				@Option(name = "--nearest-neighbours", usage = "number of neighbours", required = false)
				int K = 1;

				@Option(name = "--distance", usage = "Distance function", required = false)
				DoubleFVComparison comparison = DoubleFVComparison.EUCLIDEAN;

				@Option(name = "--threshold", usage = "Distance threshold", required = false)
				float threshold = Float.NaN;

				@Override
				public FaceRecognitionEngine<?, ?, String> createRecognitionEngine() {
					if (threshold == Float.NaN)
						threshold = comparison.isDistance() ? Float.MAX_VALUE : -Float.MAX_VALUE;

					final EigenFaceRecogniser<DetectedFace, Integer> recogniser = EigenFaceRecogniser.create(
							numComponents, alignerOp.getAligner(), K, comparison, threshold);

					return new FaceRecognitionEngine(alignerOp.getDetector(), recogniser);
				}
			};
		}
	},
	/**
	 * FisherFaces with a KNN classifier
	 */
	FisherFaces_KNN {
		@Override
		public RecognitionEngineProvider getOptions() {
			return new RecognitionEngineProvider() {
				@Option(name = "--num-components", usage = "number of components")
				int numComponents = 10;

				@SuppressWarnings("unused")
				@Option(name = "--aligner", usage = "aligner", required = false, handler = ProxyOptionHandler.class)
				AnyAligner aligner = AnyAligner.Identity;
				AlignerDetectorProvider alignerOp;

				@Option(name = "--nearest-neighbours", usage = "number of neighbours", required = false)
				int K = 1;

				@Option(name = "--distance", usage = "Distance function", required = false)
				DoubleFVComparison comparison = DoubleFVComparison.EUCLIDEAN;

				@Option(name = "--threshold", usage = "Distance threshold", required = false)
				float threshold = Float.NaN;

				@Override
				public FaceRecognitionEngine<?, ?, String> createRecognitionEngine() {
					if (threshold == Float.NaN)
						threshold = comparison.isDistance() ? Float.MAX_VALUE : -Float.MAX_VALUE;

					final FisherFaceRecogniser<DetectedFace, Integer> recogniser = FisherFaceRecogniser.create(
							numComponents, alignerOp.getAligner(), K, comparison, threshold);

					return new FaceRecognitionEngine(alignerOp.getDetector(), recogniser);
				}
			};
		}
	},
	/**
	 * Pixel patches around facial keypoints
	 */
	FacePatch_KNN {
		@Override
		public RecognitionEngineProvider getOptions() {
			return new RecognitionEngineProvider() {
				@Option(name = "--nearest-neighbours", usage = "number of neighbours", required = false)
				int K = 1;

				@Option(name = "--distance", usage = "Distance function", required = false)
				FloatFVComparison comparison = FloatFVComparison.EUCLIDEAN;

				@Option(name = "--threshold", usage = "Distance threshold", required = false)
				float threshold = Float.NaN;

				@Override
				public <FACE extends DetectedFace> FaceRecognitionEngine<FACE, ?, String> createRecognitionEngine() {
					if (threshold == Float.NaN)
						threshold = comparison.isDistance() ? Float.MAX_VALUE : -Float.MAX_VALUE;

					final FacePatchFeature.Extractor extractor = new FacePatchFeature.Extractor();
					final FacialFeatureComparator<FacePatchFeature> comparator = new FaceFVComparator<FacePatchFeature>(
							comparison);

					final AnnotatorFaceRecogniser<KEDetectedFace, FacePatchFeature.Extractor, String> recogniser = AnnotatorFaceRecogniser
							.create(new KNNAnnotator<KEDetectedFace, String, FacePatchFeature.Extractor, FacePatchFeature>(
									extractor, comparator, K, threshold));

					final FKEFaceDetector detector = new FKEFaceDetector();

					return new FaceRecognitionEngine(detector, recogniser);
				}
			};
		}
	},
	/**
	 * Local Binary Pattern histograms with KNN classifier
	 */
	LBPHistograms_KNN {
		@Override
		public RecognitionEngineProvider getOptions() {
			return new RecognitionEngineProvider() {
				@SuppressWarnings("unused")
				@Option(name = "--aligner", usage = "aligner", required = false, handler = ProxyOptionHandler.class)
				AnyAligner aligner = AnyAligner.Identity;
				AlignerDetectorProvider alignerOp;

				@Option(name = "--nearest-neighbours", usage = "number of neighbours", required = false)
				int K = 1;

				@Option(name = "--distance", usage = "Distance function", required = false)
				FloatFVComparison comparison = FloatFVComparison.EUCLIDEAN;

				@Option(name = "--threshold", usage = "Distance threshold", required = false)
				float threshold = Float.NaN;

				@Option(name = "--blocksX", usage = "The number of blocks in the x-direction", required = false)
				int blocksX = 20;

				@Option(name = "--blocksY", usage = "The number of blocks in the y-direction", required = false)
				int blocksY = 20;

				@Option(name = "--samples", usage = "The number of samples around a sampling circle", required = false)
				int samples = 8;

				@Option(name = "--radius", usage = "The radius of the sampling circle", required = false)
				int radius = 1;

				@Override
				public <FACE extends DetectedFace> FaceRecognitionEngine<FACE, ?, String> createRecognitionEngine() {
					if (threshold == Float.NaN)
						threshold = comparison.isDistance() ? Float.MAX_VALUE : -Float.MAX_VALUE;

					final LocalLBPHistogram.Extractor<KEDetectedFace> extractor = new LocalLBPHistogram.Extractor<KEDetectedFace>(
							alignerOp.getAligner(), blocksX, blocksY, samples, radius);
					final FacialFeatureComparator<LocalLBPHistogram> comparator = new FaceFVComparator<LocalLBPHistogram>(
							comparison);

					final AnnotatorFaceRecogniser recogniser = AnnotatorFaceRecogniser.create(new KNNAnnotator(extractor,
							comparator, K, threshold));

					return FaceRecognitionEngine.create(alignerOp.getDetector(), recogniser);
				}
			};
		}
	},
	/**
	 * Local Binary Pattern histograms with Naive Bayes classifier
	 */
	LBPHistograms_NaiveBayes {
		@Override
		public RecognitionEngineProvider getOptions() {
			return new RecognitionEngineProvider() {
				@SuppressWarnings("unused")
				@Option(name = "--aligner", usage = "aligner", required = false, handler = ProxyOptionHandler.class)
				AnyAligner aligner = AnyAligner.Identity;
				AlignerDetectorProvider alignerOp;

				@Option(name = "--blocksX", usage = "The number of blocks in the x-direction", required = false)
				int blocksX = 20;

				@Option(name = "--blocksY", usage = "The number of blocks in the y-direction", required = false)
				int blocksY = 20;

				@Option(name = "--samples", usage = "The number of samples around a sampling circle", required = false)
				int samples = 8;

				@Option(name = "--radius", usage = "The radius of the sampling circle", required = false)
				int radius = 1;

				@Override
				public <FACE extends DetectedFace> FaceRecognitionEngine<FACE, ?, String> createRecognitionEngine() {
					final LocalLBPHistogram.Extractor<KEDetectedFace> extractor = new LocalLBPHistogram.Extractor<KEDetectedFace>(
							alignerOp.getAligner(), blocksX, blocksY, samples, radius);

					final FVProviderExtractor extractor2 = new FVProviderExtractor(extractor);

					final AnnotatorFaceRecogniser recogniser = AnnotatorFaceRecogniser.create(new NaiveBayesAnnotator(
							extractor2));

					return FaceRecognitionEngine.create(alignerOp.getDetector(), recogniser);
				}
			};
		}
	},
	;

	@Override
	public abstract RecognitionEngineProvider getOptions();
}
