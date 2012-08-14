package org.openimaj.tools.faces.recognition.options;

import org.kohsuke.args4j.CmdLineOptionsProvider;
import org.kohsuke.args4j.Option;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.FaceDetector;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.image.processing.face.detection.IdentityFaceDetector;

/**
 * Face detector configuration for the tools
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class FaceDetectors {
	/**
	 * Interface for providers of {@link FaceDetector}s
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * 
	 * @param <FACE>
	 *            type of {@link DetectedFace}
	 * @param <IMAGE>
	 *            type of {@link Image} that the detector works on
	 */
	public interface FaceDetectorProvider<FACE extends DetectedFace, IMAGE extends Image<?, IMAGE>> {
		/**
		 * @return the detector
		 */
		public FaceDetector<FACE, IMAGE> getDetector();
	}

	/**
	 * Any type of basic {@link FImage} detector. Doesn't include the enhanced
	 * detectors that wrap other detectors.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * 
	 */
	public enum AnyBasicFImageDetector implements CmdLineOptionsProvider {
		/**
		 * @see IdentityFaceDetector
		 */
		Identity {
			@Override
			public FaceDetectorProvider<DetectedFace, FImage> getOptions() {
				return new IdentityProvider<FImage>();
			}
		},
		/**
		 * @see HaarCascadeDetector
		 */
		Haar {
			@Override
			public FaceDetectorProvider<DetectedFace, FImage> getOptions() {
				return new HaarCascadeProvider();
			}
		},
		;

		@Override
		public abstract FaceDetectorProvider<DetectedFace, FImage> getOptions();
	}

	private static class IdentityProvider<IMAGE extends Image<?, IMAGE>>
			implements
				FaceDetectorProvider<DetectedFace, IMAGE>
	{
		@Override
		public FaceDetector<DetectedFace, IMAGE> getDetector() {
			return new IdentityFaceDetector<IMAGE>();
		}
	}

	private static class HaarCascadeProvider implements FaceDetectorProvider<DetectedFace, FImage> {
		@Option(name = "--cascade", usage = "Haar Cascade", required = false)
		HaarCascadeDetector.BuiltInCascade cascade = HaarCascadeDetector.BuiltInCascade.frontalface_alt2;

		@Option(name = "--min-size", usage = "Minimum detection size", required = false)
		int minSize = 80;

		@Override
		public FaceDetector<DetectedFace, FImage> getDetector() {
			final HaarCascadeDetector fd = cascade.load();
			fd.setMinSize(minSize);

			return fd;
		}
	}
}
