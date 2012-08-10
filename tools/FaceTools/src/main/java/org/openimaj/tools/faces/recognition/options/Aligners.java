package org.openimaj.tools.faces.recognition.options;

import org.kohsuke.args4j.CmdLineOptionsProvider;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ProxyOptionHandler;
import org.openimaj.image.processing.face.alignment.FaceAligner;
import org.openimaj.image.processing.face.alignment.IdentityAligner;
import org.openimaj.image.processing.face.detection.FaceDetector;
import org.openimaj.tools.faces.recognition.options.FaceDetectors.AnyDetector;
import org.openimaj.tools.faces.recognition.options.FaceDetectors.FaceDetectorProvider;

public class Aligners {
	public interface AlignerDetectorProvider {
		public FaceAligner getAligner();
		public FaceDetector getDetector();
	}

	public enum AnyAligner implements CmdLineOptionsProvider {
		Identity {
			@Override
			public AlignerDetectorProvider getOptions() {
				return new Identity();
			}
		}
		;

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
}