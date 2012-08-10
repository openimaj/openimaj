package org.openimaj.tools.faces.recognition.options;

import org.kohsuke.args4j.CmdLineOptionsProvider;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ProxyOptionHandler;
import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.recognition.EigenFaceRecogniser;
import org.openimaj.image.processing.face.recognition.FaceRecognitionEngine;
import org.openimaj.image.processing.face.recognition.FisherFaceRecogniser;
import org.openimaj.tools.faces.recognition.options.Aligners.AlignerDetectorProvider;
import org.openimaj.tools.faces.recognition.options.Aligners.AnyAligner;

/**
 * Standard recognition strategies
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public enum RecognitionStrategy implements CmdLineOptionsProvider {
	/**
	 * EigenFaces with a KNN classifier
	 */
	EigenFaces_KNN {
		@Override
		public EngineProvider getOptions() {
			return new EngineProvider() {
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
					
					EigenFaceRecogniser<DetectedFace, Integer> recogniser = 
						EigenFaceRecogniser.create(numComponents, alignerOp.getAligner(), K, comparison, threshold);
					
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
		public EngineProvider getOptions() {
			return new EngineProvider() {
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
					
					FisherFaceRecogniser<DetectedFace, Integer> recogniser = 
						FisherFaceRecogniser.create(numComponents, alignerOp.getAligner(), K, comparison, threshold);
					
					return new FaceRecognitionEngine(alignerOp.getDetector(), recogniser);
				}
			};
		}
	},

	;

	@Override
	public abstract EngineProvider getOptions();
}