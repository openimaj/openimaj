package org.openimaj.tools.faces.recognition.options;

import org.kohsuke.args4j.CmdLineOptionsProvider;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ProxyOptionHandler;
import org.openimaj.image.processing.face.alignment.AffineAligner;
import org.openimaj.image.processing.face.alignment.FaceAligner;
import org.openimaj.image.processing.face.alignment.IdentityAligner;
import org.openimaj.image.processing.face.alignment.MeshWarpAligner;
import org.openimaj.image.processing.face.alignment.RotateScaleAligner;
import org.openimaj.image.processing.face.alignment.ScalingAligner;
import org.openimaj.image.processing.face.detection.FaceDetector;
import org.openimaj.image.processing.face.detection.keypoints.FKEFaceDetector;
import org.openimaj.tools.faces.recognition.options.FaceDetectors.AnyDetector;
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
	 */
	public interface AlignerDetectorProvider {
		/**
		 * @return the aligner
		 */
		public FaceAligner getAligner();

		/**
		 * @return the detector
		 */
		public FaceDetector getDetector();
	}

	public enum AnyAligner implements CmdLineOptionsProvider {
		Identity {
			@Override
			public AlignerDetectorProvider getOptions() {
				return new Identity();
			}
		},
		Affine {
			@Override
			public AlignerDetectorProvider getOptions() {
				return new Affine();
			}
		},
		MeshWarp {
			@Override
			public AlignerDetectorProvider getOptions() {
				return new MeshWarp();
			}
		},
		RotateScale {
			@Override
			public AlignerDetectorProvider getOptions() {
				return new RotateScale();
			}
		},
		;

		@Override
		public abstract AlignerDetectorProvider getOptions();
	}

	private static class Identity implements AlignerDetectorProvider {
		@SuppressWarnings("unused")
		@Option(name = "--detector", usage = "Face detector", required = true, handler = ProxyOptionHandler.class)
		AnyDetector detector;
		FaceDetectorProvider detectorOp;

		@Override
		public FaceAligner getAligner() {
			return new IdentityAligner();
		}

		@Override
		public FaceDetector getDetector() {
			return detectorOp.getDetector();
		}
	}

	private static class Scaling implements AlignerDetectorProvider {
		@SuppressWarnings("unused")
		@Option(name = "--detector", usage = "Face detector", required = true, handler = ProxyOptionHandler.class)
		AnyDetector detector;
		FaceDetectorProvider detectorOp;

		@Option(name = "--width", usage = "Aligner patch output width", required = false)
		int width = 100;

		@Option(name = "--height", usage = "Aligner patch output height", required = false)
		int height = 100;

		@Override
		public FaceAligner getAligner() {
			return new ScalingAligner(width, height);
		}

		@Override
		public FaceDetector getDetector() {
			return detectorOp.getDetector();
		}
	}

	private static class Affine implements AlignerDetectorProvider {
		@SuppressWarnings("unused")
		@Option(name = "--detector", usage = "Face detector", required = true, handler = ProxyOptionHandler.class)
		AnyDetector detector;
		FaceDetectorProvider detectorOp;

		@Override
		public FaceAligner getAligner() {
			return new AffineAligner();
		}

		@Override
		public FaceDetector getDetector() {
			return new FKEFaceDetector(detectorOp.getDetector());
		}
	}

	private static class MeshWarp implements AlignerDetectorProvider {
		@SuppressWarnings("unused")
		@Option(name = "--detector", usage = "Face detector", required = true, handler = ProxyOptionHandler.class)
		AnyDetector detector;
		FaceDetectorProvider detectorOp;

		@Override
		public FaceAligner getAligner() {
			return new MeshWarpAligner();
		}

		@Override
		public FaceDetector getDetector() {
			return new FKEFaceDetector(detectorOp.getDetector());
		}
	}

	private static class RotateScale implements AlignerDetectorProvider {
		@SuppressWarnings("unused")
		@Option(name = "--detector", usage = "Face detector", required = true, handler = ProxyOptionHandler.class)
		AnyDetector detector;
		FaceDetectorProvider detectorOp;

		@Override
		public FaceAligner getAligner() {
			return new RotateScaleAligner();
		}

		@Override
		public FaceDetector getDetector() {
			return new FKEFaceDetector(detectorOp.getDetector());
		}
	}
}
