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
