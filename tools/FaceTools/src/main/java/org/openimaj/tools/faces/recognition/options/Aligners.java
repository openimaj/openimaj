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
