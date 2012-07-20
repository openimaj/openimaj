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
package org.openimaj.image.processing.face.feature.ltp;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.image.FImage;
import org.openimaj.image.processing.face.alignment.FaceAligner;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.feature.FacialFeatureExtractor;
import org.openimaj.io.IOUtils;

/**
 * The LTP based feature using a truncated Euclidean distance transform
 * to estimate the distances within each slice.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Reference(
		type = ReferenceType.Article,
		author = { "Xiaoyang Tan", "Triggs, B." },
		title = "Enhanced Local Texture Feature Sets for Face Recognition Under Difficult Lighting Conditions",
		year = "2010",
		journal = "Image Processing, IEEE Transactions on",
		pages = { "1635 ", "1650" },
		month = "june ",
		number = "6",
		volume = "19",
		customData = {
			"keywords", "CAS-PEAL-R1;Gabor wavelets;PCA;distance transform based matching;extended Yale-B;face recognition;kernel-based feature extraction;local binary patterns;local spatial histograms;local ternary patterns;local texture feature set enhancement;local texture-based face representations;multiple feature fusion;principal component analysis;robust illumination normalization;face recognition;feature extraction;image enhancement;image fusion;image representation;image texture;principal component analysis;wavelet transforms;Algorithms;Biometry;Face;Humans;Image Enhancement;Image Interpretation, Computer-Assisted;Imaging, Three-Dimensional;Lighting;Pattern Recognition, Automated;Reproducibility of Results;Sensitivity and Specificity;Subtraction Technique;",
			"doi", "10.1109/TIP.2010.2042645",
			"ISSN", "1057-7149"
		}
	)
public class LtpDtFeature extends AbstractLtpDtFeature {
	/**
	 * A {@link FacialFeatureExtractor} for extracting {@link LtpDtFeature}s.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 *
	 * @param <Q> Type of detected face 
	 */
	public static class Extractor<Q extends DetectedFace> implements FacialFeatureExtractor<LtpDtFeature, Q> {
		LTPWeighting weighting;
		FaceAligner<Q> aligner;
		
		protected Extractor() {}
		
		/**
		 * Construct the extractor with the given face aligner and weighting scheme.
		 * @param aligner the aligner.
		 * @param weighting the weighting scheme.
		 */
		public Extractor(FaceAligner<Q> aligner, LTPWeighting weighting) {
			this.aligner = aligner;
			this.weighting = weighting;
		}
			
		@Override
		public LtpDtFeature extractFeature(Q detectedFace) {
			FImage face = aligner.align(detectedFace);
			FImage mask = aligner.getMask();
			
			return new LtpDtFeature(face, mask, weighting);
		}

		@Override
		public void readBinary(DataInput in) throws IOException {
			String weightingClass = in.readUTF();
			weighting = IOUtils.newInstance(weightingClass);
			weighting.readBinary(in);
			
			String alignerClass = in.readUTF();
			aligner = IOUtils.newInstance(alignerClass);
			aligner.readBinary(in);
		}

		@Override
		public byte[] binaryHeader() {
			return this.getClass().getName().getBytes();
		}

		@Override
		public void writeBinary(DataOutput out) throws IOException {
			out.writeUTF(weighting.getClass().getName());
			weighting.writeBinary(out);
			
			out.writeUTF(aligner.getClass().getName());
			aligner.writeBinary(out);
		}
		
		@Override
		public String toString() {
			return "LtpDtFeature.Factory[weighting="+weighting+"]";
		}
	}

	/**
	 * Construct a {@link LtpDtFeature} feature.
	 * 
	 * @param face the aligned face image
	 * @param mask the mask
	 * @param weighting the weighting scheme
	 */
	public LtpDtFeature(FImage face, FImage mask, LTPWeighting weighting) {
		super(face.width, face.height, weighting, extractLTPSlicePixels(normaliseImage(face, mask)));
	}
}
