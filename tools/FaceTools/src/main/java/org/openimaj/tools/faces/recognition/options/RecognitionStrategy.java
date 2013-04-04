/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.tools.faces.recognition.options;

import org.kohsuke.args4j.CmdLineOptionsProvider;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ProxyOptionHandler;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.feature.FVProviderExtractor;
import org.openimaj.feature.FloatFV;
import org.openimaj.feature.FloatFVComparison;
import org.openimaj.image.processing.face.detection.CLMDetectedFace;
import org.openimaj.image.processing.face.detection.CLMFaceDetector;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.keypoints.FKEFaceDetector;
import org.openimaj.image.processing.face.detection.keypoints.KEDetectedFace;
import org.openimaj.image.processing.face.feature.CLMPoseFeature;
import org.openimaj.image.processing.face.feature.CLMPoseShapeFeature;
import org.openimaj.image.processing.face.feature.CLMShapeFeature;
import org.openimaj.image.processing.face.feature.FacePatchFeature;
import org.openimaj.image.processing.face.feature.LocalLBPHistogram;
import org.openimaj.image.processing.face.feature.comparison.FaceFVComparator;
import org.openimaj.image.processing.face.feature.comparison.FacialFeatureComparator;
import org.openimaj.image.processing.face.recognition.AnnotatorFaceRecogniser;
import org.openimaj.image.processing.face.recognition.EigenFaceRecogniser;
import org.openimaj.image.processing.face.recognition.FaceRecognitionEngine;
import org.openimaj.image.processing.face.recognition.FisherFaceRecogniser;
import org.openimaj.ml.annotation.InstanceCachingIncrementalBatchAnnotator;
import org.openimaj.ml.annotation.basic.KNNAnnotator;
import org.openimaj.ml.annotation.bayes.NaiveBayesAnnotator;
import org.openimaj.ml.annotation.linear.LinearSVMAnnotator;
import org.openimaj.tools.faces.recognition.options.Aligners.AlignerDetectorProvider;
import org.openimaj.tools.faces.recognition.options.Aligners.AnyAligner;

/**
 * Standard recognition strategies
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public enum RecognitionStrategy implements CmdLineOptionsProvider {
	/**
	 * EigenFaces with a KNN classifier
	 */
	EigenFaces_KNN {
		@Override
		public RecognitionEngineProvider<DetectedFace> getOptions() {
			return new RecognitionEngineProvider<DetectedFace>() {
				@Option(name = "--num-components", usage = "number of components")
				int numComponents = 10;

				@Option(name = "--aligner", usage = "aligner", required = false, handler = ProxyOptionHandler.class)
				AnyAligner aligner = AnyAligner.Identity;
				AlignerDetectorProvider<DetectedFace> alignerOp;

				@Option(name = "--nearest-neighbours", usage = "number of neighbours", required = false)
				int K = 1;

				@Option(name = "--distance", usage = "Distance function", required = false)
				DoubleFVComparison comparison = DoubleFVComparison.EUCLIDEAN;

				@Option(name = "--threshold", usage = "Distance threshold", required = false)
				float threshold = Float.NaN;

				@Override
				public FaceRecognitionEngine<DetectedFace, String> createRecognitionEngine() {
					if (threshold == Float.NaN)
						threshold = comparison.isDistance() ? Float.MAX_VALUE : -Float.MAX_VALUE;

					final EigenFaceRecogniser<DetectedFace, String> recogniser = EigenFaceRecogniser.create(
							numComponents, alignerOp.getAligner(), K, comparison, threshold);

					return FaceRecognitionEngine.create(alignerOp.getDetector(), recogniser);
				}
			};
		}
	},
	/**
	 * FisherFaces with a KNN classifier
	 */
	FisherFaces_KNN {
		@Override
		public RecognitionEngineProvider<DetectedFace> getOptions() {
			return new RecognitionEngineProvider<DetectedFace>() {
				@Option(name = "--num-components", usage = "number of components")
				int numComponents = 10;

				@Option(name = "--aligner", usage = "aligner", required = false, handler = ProxyOptionHandler.class)
				AnyAligner aligner = AnyAligner.Identity;
				AlignerDetectorProvider<DetectedFace> alignerOp;

				@Option(name = "--nearest-neighbours", usage = "number of neighbours", required = false)
				int K = 1;

				@Option(name = "--distance", usage = "Distance function", required = false)
				DoubleFVComparison comparison = DoubleFVComparison.EUCLIDEAN;

				@Option(name = "--threshold", usage = "Distance threshold", required = false)
				float threshold = Float.NaN;

				@Override
				public FaceRecognitionEngine<DetectedFace, String> createRecognitionEngine() {
					if (threshold == Float.NaN)
						threshold = comparison.isDistance() ? Float.MAX_VALUE : -Float.MAX_VALUE;

					final FisherFaceRecogniser<DetectedFace, String> recogniser = FisherFaceRecogniser.create(
							numComponents, alignerOp.getAligner(), K, comparison, threshold);

					return FaceRecognitionEngine.create(alignerOp.getDetector(), recogniser);
				}
			};
		}
	},
	/**
	 * Pixel patches around facial keypoints
	 */
	FacePatch_KNN {
		@Override
		public RecognitionEngineProvider<KEDetectedFace> getOptions() {
			return new RecognitionEngineProvider<KEDetectedFace>() {
				@Option(name = "--nearest-neighbours", usage = "number of neighbours", required = false)
				int K = 1;

				@Option(name = "--distance", usage = "Distance function", required = false)
				FloatFVComparison comparison = FloatFVComparison.EUCLIDEAN;

				@Option(name = "--threshold", usage = "Distance threshold", required = false)
				float threshold = Float.NaN;

				@Override
				public FaceRecognitionEngine<KEDetectedFace, String> createRecognitionEngine() {
					if (threshold == Float.NaN)
						threshold = comparison.isDistance() ? Float.MAX_VALUE : -Float.MAX_VALUE;

					final FacePatchFeature.Extractor extractor = new FacePatchFeature.Extractor();
					final FacialFeatureComparator<FacePatchFeature> comparator = new FaceFVComparator<FacePatchFeature, FloatFV>(
							comparison);

					final AnnotatorFaceRecogniser<KEDetectedFace, String> recogniser = AnnotatorFaceRecogniser
							.create(new KNNAnnotator<KEDetectedFace, String, FacePatchFeature>(
									extractor, comparator, K, threshold));

					final FKEFaceDetector detector = new FKEFaceDetector();

					return FaceRecognitionEngine.create(detector, recogniser);
				}
			};
		}
	},
	/**
	 * Local Binary Pattern histograms with KNN classifier
	 */
	LBPHistograms_KNN {
		@Override
		public RecognitionEngineProvider<DetectedFace> getOptions() {
			return new LBPHistogramBaseOptions() {
				@Option(name = "--nearest-neighbours", usage = "number of neighbours", required = false)
				int K = 1;

				@Option(name = "--distance", usage = "Distance function", required = false)
				FloatFVComparison comparison = FloatFVComparison.EUCLIDEAN;

				@Option(name = "--threshold", usage = "Distance threshold", required = false)
				float threshold = Float.NaN;

				@Override
				public FaceRecognitionEngine<DetectedFace, String> createRecognitionEngine() {
					if (threshold == Float.NaN)
						threshold = comparison.isDistance() ? Float.MAX_VALUE : -Float.MAX_VALUE;

					final LocalLBPHistogram.Extractor<DetectedFace> extractor = new LocalLBPHistogram.Extractor<DetectedFace>(
							alignerOp.getAligner(), blocksX, blocksY, samples, radius);
					final FacialFeatureComparator<LocalLBPHistogram> comparator = new FaceFVComparator<LocalLBPHistogram, FloatFV>(
							comparison);

					final KNNAnnotator<DetectedFace, String, LocalLBPHistogram> knn =
							KNNAnnotator.create(extractor, comparator, K, threshold);

					final AnnotatorFaceRecogniser<DetectedFace, String> recogniser =
							AnnotatorFaceRecogniser.create(knn);

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
		public RecognitionEngineProvider<DetectedFace> getOptions() {
			return new LBPHistogramBaseOptions() {
				@Override
				public FaceRecognitionEngine<DetectedFace, String> createRecognitionEngine() {
					final LocalLBPHistogram.Extractor<DetectedFace> extractor =
							new LocalLBPHistogram.Extractor<DetectedFace>(alignerOp.getAligner(), blocksX, blocksY,
									samples, radius);

					final FVProviderExtractor<FloatFV, DetectedFace> extractor2 =
							FVProviderExtractor.create(extractor);

					final NaiveBayesAnnotator<DetectedFace, String> bayes =
							new NaiveBayesAnnotator<DetectedFace, String>(
									extractor2, NaiveBayesAnnotator.Mode.MAXIMUM_LIKELIHOOD);

					final AnnotatorFaceRecogniser<DetectedFace, String> recogniser =
							AnnotatorFaceRecogniser.create(bayes);

					return FaceRecognitionEngine.create(alignerOp.getDetector(), recogniser);
				}
			};
		}
	},
	/**
	 * Local Binary Pattern histograms with Linear SVM classifier
	 */
	LBPHistograms_LinearSVM {
		@Override
		public RecognitionEngineProvider<DetectedFace> getOptions() {
			return new LBPHistogramBaseOptions() {
				@Override
				public FaceRecognitionEngine<DetectedFace, String> createRecognitionEngine() {
					final LocalLBPHistogram.Extractor<DetectedFace> extractor =
							new LocalLBPHistogram.Extractor<DetectedFace>(alignerOp.getAligner(), blocksX, blocksY,
									samples, radius);

					final FVProviderExtractor<FloatFV, DetectedFace> extractor2 =
							FVProviderExtractor.create(extractor);

					final LinearSVMAnnotator<DetectedFace, String> svm = new LinearSVMAnnotator<DetectedFace, String>(
							extractor2);

					final InstanceCachingIncrementalBatchAnnotator<DetectedFace, String> wrapper =
							new InstanceCachingIncrementalBatchAnnotator<DetectedFace, String>(svm);

					final AnnotatorFaceRecogniser<DetectedFace, String> recogniser = AnnotatorFaceRecogniser
							.create(wrapper);

					return FaceRecognitionEngine.create(alignerOp.getDetector(), recogniser);
				}
			};
		}
	},
	/**
	 * Shape and/or pose features from a {@link CLMDetectedFace} coupled with a
	 * KNN classifier.
	 */
	CLMFeature_KNN {
		@Option(name = "--nearest-neighbours", usage = "number of neighbours", required = false)
		int K = 1;

		@Option(name = "--distance", usage = "Distance function", required = false)
		DoubleFVComparison comparison = DoubleFVComparison.EUCLIDEAN;

		@Option(name = "--threshold", usage = "Distance threshold", required = false)
		float threshold = Float.NaN;

		@Override
		public RecognitionEngineProvider<?> getOptions() {
			return new CLMFeaturesBaseOptions() {
				@Override
				public FaceRecognitionEngine<CLMDetectedFace, String> createRecognitionEngine() {
					final FVProviderExtractor<DoubleFV, CLMDetectedFace> extractor = getWrapperExtractor();

					final KNNAnnotator<CLMDetectedFace, String, DoubleFV> knn =
							KNNAnnotator.create(extractor, comparison, K, threshold);

					final AnnotatorFaceRecogniser<CLMDetectedFace, String> recogniser = AnnotatorFaceRecogniser
							.create(knn);

					return FaceRecognitionEngine.create(new CLMFaceDetector(), recogniser);
				}
			};
		}
	},
	/**
	 * Shape and/or pose features from a {@link CLMDetectedFace} coupled with a
	 * Naive Bayes classifier.
	 */
	CLMFeature_NaiveBayes {
		@Override
		public RecognitionEngineProvider<?> getOptions() {
			return new CLMFeaturesBaseOptions() {
				@Override
				public FaceRecognitionEngine<CLMDetectedFace, String> createRecognitionEngine() {
					final FVProviderExtractor<DoubleFV, CLMDetectedFace> extractor = getWrapperExtractor();

					final NaiveBayesAnnotator<CLMDetectedFace, String> bayes =
							new NaiveBayesAnnotator<CLMDetectedFace, String>(
									extractor, NaiveBayesAnnotator.Mode.MAXIMUM_LIKELIHOOD);

					final AnnotatorFaceRecogniser<CLMDetectedFace, String> recogniser = AnnotatorFaceRecogniser
							.create(bayes);

					return FaceRecognitionEngine.create(new CLMFaceDetector(), recogniser);
				}
			};
		}
	};

	@Override
	public abstract RecognitionEngineProvider<?> getOptions();

	private static abstract class LBPHistogramBaseOptions implements RecognitionEngineProvider<DetectedFace> {
		@Option(name = "--aligner", usage = "aligner", required = false, handler = ProxyOptionHandler.class)
		AnyAligner aligner = AnyAligner.Identity;
		AlignerDetectorProvider<DetectedFace> alignerOp;

		@Option(name = "--blocksX", usage = "The number of blocks in the x-direction", required = false)
		int blocksX = 20;

		@Option(name = "--blocksY", usage = "The number of blocks in the y-direction", required = false)
		int blocksY = 20;

		@Option(name = "--samples", usage = "The number of samples around a sampling circle", required = false)
		int samples = 8;

		@Option(name = "--radius", usage = "The radius of the sampling circle", required = false)
		int radius = 1;
	}

	private static abstract class CLMFeaturesBaseOptions implements RecognitionEngineProvider<CLMDetectedFace> {
		public static enum Mode {
			POSE, SHAPE, POSE_SHAPE
		}

		@Option(name = "--mode", usage = "The type of CLM feature (defaults to SHAPE)", required = false)
		Mode mode = Mode.SHAPE;

		protected FVProviderExtractor<DoubleFV, CLMDetectedFace> getWrapperExtractor() {
			switch (mode) {
			case POSE: {
				final CLMPoseFeature.Extractor extr = new CLMPoseFeature.Extractor();
				return FVProviderExtractor.create(extr);
			}
			case SHAPE: {
				final CLMShapeFeature.Extractor extr = new CLMShapeFeature.Extractor();
				return FVProviderExtractor.create(extr);
			}
			case POSE_SHAPE: {
				final CLMPoseShapeFeature.Extractor extr = new CLMPoseShapeFeature.Extractor();
				return FVProviderExtractor.create(extr);
			}
			}

			return null;
		}
	}
}
