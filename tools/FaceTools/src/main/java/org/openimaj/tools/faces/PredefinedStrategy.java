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
package org.openimaj.tools.faces;

import org.openimaj.feature.FloatFV;
import org.openimaj.feature.FloatFVComparison;
import org.openimaj.image.FImage;
import org.openimaj.image.processing.face.alignment.AffineAligner;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.FaceDetector;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.image.processing.face.detection.keypoints.FKEFaceDetector;
import org.openimaj.image.processing.face.detection.keypoints.KEDetectedFace;
import org.openimaj.image.processing.face.feature.DoGSIFTFeature;
import org.openimaj.image.processing.face.feature.FacePatchFeature;
import org.openimaj.image.processing.face.feature.FacialFeatureExtractor;
import org.openimaj.image.processing.face.feature.LocalLBPHistogram;
import org.openimaj.image.processing.face.feature.comparison.DoGSIFTFeatureComparator;
import org.openimaj.image.processing.face.feature.comparison.FaceFVComparator;
import org.openimaj.image.processing.face.feature.comparison.FacialFeatureComparator;
import org.openimaj.image.processing.face.feature.comparison.ReversedLtpDtFeatureComparator;
import org.openimaj.image.processing.face.feature.ltp.LtpDtFeature;
import org.openimaj.image.processing.face.feature.ltp.TruncatedWeighting;
import org.openimaj.image.processing.face.similarity.FaceSimilarityEngine;

public enum PredefinedStrategy {
	SIFT {
		@Override
		public FaceSimilarityEngine<?, ?, FImage> strategy() {
			final FacialFeatureComparator<DoGSIFTFeature> comparator = new DoGSIFTFeatureComparator();
			final FaceDetector<DetectedFace, FImage> detector = new HaarCascadeDetector(80);
			final FacialFeatureExtractor<DoGSIFTFeature, DetectedFace> factory = new DoGSIFTFeature.Extractor();

			return FaceSimilarityEngine.create(detector, factory, comparator);
		}

		@Override
		public String description() {
			return "SIFT features using a TransformedOneToOnePointModel for feature matching and the SIFT vector for comparison.";
		}
	},
	LOCAL_TRINARY_PATTERN {
		@Override
		public FaceSimilarityEngine<?, ?, FImage> strategy() {
			final FacialFeatureComparator<LtpDtFeature> comparator = new ReversedLtpDtFeatureComparator();
			final FKEFaceDetector detector = new FKEFaceDetector();
			final FacialFeatureExtractor<LtpDtFeature, KEDetectedFace> factory =
					new LtpDtFeature.Extractor<KEDetectedFace>(
							new AffineAligner(),
							new TruncatedWeighting()
					);

			return FaceSimilarityEngine.create(detector, factory, comparator);
		}

		@Override
		public String description() {
			return "Local Ternary Pattern feature using truncated distance-maps for comparison. Faces aligned using affine transform.";
		}
	},
	FACEPATCH_EUCLIDEAN {
		@Override
		public FaceSimilarityEngine<?, ?, FImage> strategy() {
			final FacialFeatureExtractor<FacePatchFeature, KEDetectedFace> factory = new FacePatchFeature.Extractor();
			final FacialFeatureComparator<FacePatchFeature> comparator = new FaceFVComparator<FacePatchFeature, FloatFV>(
					FloatFVComparison.EUCLIDEAN);
			final FKEFaceDetector detector = new FKEFaceDetector();

			return FaceSimilarityEngine.create(detector, factory, comparator);
		}

		@Override
		public String description() {
			return "Patched facial features, compared as a big vector using Euclidean distance.";
		}
	},
	LOCAL_BINARY_PATTERN {
		@Override
		public FaceSimilarityEngine<?, ?, FImage> strategy() {
			// FacialFeatureFactory<LocalLBPHistogram, KEDetectedFace> factory =
			// new LocalLBPHistogram.Factory<KEDetectedFace>(new
			// AffineAligner(), 20, 20, 8, 1);
			// FacialFeatureFactory<LocalLBPHistogram, KEDetectedFace> factory =
			// new LocalLBPHistogram.Factory<KEDetectedFace>(new
			// AffineAligner(), 7, 7, 16, 4);
			final FacialFeatureExtractor<LocalLBPHistogram, KEDetectedFace> factory = new LocalLBPHistogram.Extractor<KEDetectedFace>(
					new AffineAligner(), 7, 7, 8, 2);
			final FacialFeatureComparator<LocalLBPHistogram> comparator = new FaceFVComparator<LocalLBPHistogram, FloatFV>(
					FloatFVComparison.CHI_SQUARE);
			final FKEFaceDetector detector = new FKEFaceDetector();

			return FaceSimilarityEngine.create(detector, factory, comparator);
		}

		@Override
		public String description() {
			return "Local LBP histograms compared using Chi squared distance. Faces aligned using affine transform.";
		}

	},
	;

	public abstract FaceSimilarityEngine<?, ?, FImage> strategy();

	public abstract String description();
}
