package org.openimaj.tools.faces.recognition.options;

import org.kohsuke.args4j.CmdLineOptionsProvider;
import org.kohsuke.args4j.Option;
import org.openimaj.image.processing.face.detection.FaceDetector;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.image.processing.face.detection.IdentityFaceDetector;
import org.openimaj.image.processing.face.detection.SandeepFaceDetector;

public class FaceDetectors {
	public interface FaceDetectorProvider {
		public FaceDetector getDetector();
	}

	public enum AnyDetector implements CmdLineOptionsProvider {
		Identity {
			@Override
			public FaceDetectorProvider getOptions() {
				return new IdentityProvider();
			}
		},
		Haar {
			@Override
			public FaceDetectorProvider getOptions() {
				return new HaarCascadeProvider();
			}
		},
		Sandeep {
			@Override
			public FaceDetectorProvider getOptions() {
				return new SandeepProvider();
			}
		};

		@Override
		public abstract FaceDetectorProvider getOptions();
	}

	private static class IdentityProvider implements FaceDetectorProvider {
		@Override
		public FaceDetector getDetector() {
			return new IdentityFaceDetector();
		}
	}

	private static class HaarCascadeProvider implements FaceDetectorProvider {
		@Option(name = "--cascade", usage = "Haar Cascade", required = false)
		HaarCascadeDetector.BuiltInCascade cascade = HaarCascadeDetector.BuiltInCascade.frontalface_alt2;

		@Option(name = "--min-size", usage = "Minimum detection size", required = false)
		int minSize = 80;

		@Override
		public FaceDetector getDetector() {
			final HaarCascadeDetector fd = cascade.load();
			fd.setMinSize(minSize);

			return fd;
		}
	}

	private static class SandeepProvider implements FaceDetectorProvider {
		@Override
		public FaceDetector getDetector() {
			return new SandeepFaceDetector();
		}
	}
}
