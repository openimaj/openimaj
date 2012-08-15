package org.openimaj.tools.faces.recognition.options;

import org.kohsuke.args4j.CmdLineOptionsProvider;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ProxyOptionHandler;
import org.openimaj.image.FImage;
import org.openimaj.image.processing.face.alignment.AffineAligner;
import org.openimaj.image.processing.face.alignment.CLMAligner;
import org.openimaj.image.processing.face.alignment.FaceAligner;
import org.openimaj.image.processing.face.alignment.IdentityAligner;
import org.openimaj.image.processing.face.alignment.MeshWarpAligner;
import org.openimaj.image.processing.face.alignment.RotateScaleAligner;
import org.openimaj.image.processing.face.alignment.ScalingAligner;
import org.openimaj.image.processing.face.detection.CLMDetectedFace;
import org.openimaj.image.processing.face.detection.CLMFaceDetector;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.FaceDetector;
import org.openimaj.image.processing.face.detection.keypoints.FKEFaceDetector;
import org.openimaj.image.processing.face.detection.keypoints.KEDetectedFace;
import org.openimaj.tools.faces.recognition.options.FaceDetectors.AnyBasicFImageDetector;
import org.openimaj.tools.faces.recognition.options.FaceDetectors.FaceDetectorProvider;

/**
 * Face aligners configuration for the tools.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class Aligners {
	/**
	 * Interface for configuration objects that can provide a compatible aligner
	 * and detector pair.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * 
	 * @param <FACE>
	 *            type of {@link DetectedFace}
	 * 
	 */
	public interface AlignerDetectorProvider<FACE extends DetectedFace> {
		/**
		 * @return the aligner
		 */
		public FaceAligner<FACE> getAligner();

		/**
		 * @return the detector
		 */
		public FaceDetector<FACE, FImage> getDetector();
	}

	/**
	 * All types of aligner
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * 
	 */
	public enum AnyAligner implements CmdLineOptionsProvider {
		/**
		 * Identity aligner
		 * 
		 * @see IdentityAligner
		 */
		Identity {
			@Override
			public AlignerDetectorProvider<?> getOptions() {
				return new Identity();
			}
		},
		/**
		 * Scaling aligner
		 * 
		 * @see ScalingAligner
		 */
		Scaling {
			@Override
			public AlignerDetectorProvider<?> getOptions() {
				return new Scaling();
			}
		},
		/**
		 * Affine aligner
		 * 
		 * @see AffineAligner
		 */
		Affine {
			@Override
			public AlignerDetectorProvider<?> getOptions() {
				return new Affine();
			}
		},
		/**
		 * MeshWarp aligner
		 * 
		 * @see MeshWarpAligner
		 */
		MeshWarp {
			@Override
			public AlignerDetectorProvider<?> getOptions() {
				return new MeshWarp();
			}
		},
		/**
		 * Rotate Scale aligner
		 * 
		 * @see RotateScaleAligner
		 */
		RotateScale {
			@Override
			public AlignerDetectorProvider<?> getOptions() {
				return new RotateScale();
			}
		},
		/**
		 * CLM Aligner
		 * 
		 * @see CLMAligner
		 */
		CLM {
			@Override
			public AlignerDetectorProvider<?> getOptions() {
				return new CLM();
			}
		};

		@Override
		public abstract AlignerDetectorProvider<?> getOptions();
	}

	private static class Identity implements AlignerDetectorProvider<DetectedFace> {
		@SuppressWarnings("unused")
		@Option(name = "--detector", usage = "Face detector", required = true, handler = ProxyOptionHandler.class)
		AnyBasicFImageDetector detector;
		FaceDetectorProvider<DetectedFace, FImage> detectorOp;

		@Override
		public FaceAligner<DetectedFace> getAligner() {
			return new IdentityAligner<DetectedFace>();
		}

		@Override
		public FaceDetector<DetectedFace, FImage> getDetector() {
			return detectorOp.getDetector();
		}
	}

	private static class Scaling implements AlignerDetectorProvider<DetectedFace> {
		@SuppressWarnings("unused")
		@Option(name = "--detector", usage = "Face detector", required = true, handler = ProxyOptionHandler.class)
		AnyBasicFImageDetector detector;
		FaceDetectorProvider<DetectedFace, FImage> detectorOp;

		@Option(name = "--width", usage = "Aligner patch output width", required = false)
		int width = 100;

		@Option(name = "--height", usage = "Aligner patch output height", required = false)
		int height = 100;

		@Override
		public FaceAligner<DetectedFace> getAligner() {
			return new ScalingAligner<DetectedFace>(width, height);
		}

		@Override
		public FaceDetector<DetectedFace, FImage> getDetector() {
			return detectorOp.getDetector();
		}
	}

	private static class Affine implements AlignerDetectorProvider<KEDetectedFace> {
		@SuppressWarnings("unused")
		@Option(name = "--detector", usage = "Face detector", required = true, handler = ProxyOptionHandler.class)
		AnyBasicFImageDetector detector;
		FaceDetectorProvider<DetectedFace, FImage> detectorOp;

		@Override
		public FaceAligner<KEDetectedFace> getAligner() {
			return new AffineAligner();
		}

		@Override
		public FaceDetector<KEDetectedFace, FImage> getDetector() {
			return new FKEFaceDetector(detectorOp.getDetector());
		}
	}

	private static class MeshWarp implements AlignerDetectorProvider<KEDetectedFace> {
		@SuppressWarnings("unused")
		@Option(name = "--detector", usage = "Face detector", required = true, handler = ProxyOptionHandler.class)
		AnyBasicFImageDetector detector;
		FaceDetectorProvider<DetectedFace, FImage> detectorOp;

		@Override
		public FaceAligner<KEDetectedFace> getAligner() {
			return new MeshWarpAligner();
		}

		@Override
		public FaceDetector<KEDetectedFace, FImage> getDetector() {
			return new FKEFaceDetector(detectorOp.getDetector());
		}
	}

	private static class RotateScale implements AlignerDetectorProvider<KEDetectedFace> {
		@SuppressWarnings("unused")
		@Option(name = "--detector", usage = "Face detector", required = true, handler = ProxyOptionHandler.class)
		AnyBasicFImageDetector detector;
		FaceDetectorProvider<DetectedFace, FImage> detectorOp;

		@Override
		public FaceAligner<KEDetectedFace> getAligner() {
			return new RotateScaleAligner();
		}

		@Override
		public FaceDetector<KEDetectedFace, FImage> getDetector() {
			return new FKEFaceDetector(detectorOp.getDetector());
		}
	}

	private static class CLM implements AlignerDetectorProvider<CLMDetectedFace> {
		@Option(name = "--size", usage = "Size of aligned image", required = false)
		int size = 100;

		@Override
		public FaceAligner<CLMDetectedFace> getAligner() {
			return new CLMAligner(size);
		}

		@Override
		public FaceDetector<CLMDetectedFace, FImage> getDetector() {
			return new CLMFaceDetector();
		}
	}
}
