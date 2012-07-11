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

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.io.ReadWriteableBinary;

/**
 * 
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
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
public interface LTPWeighting extends ReadWriteableBinary {
	/**
	 * Determine the weighting scheme for the distances produced
	 * by the EuclideanDistanceTransform.
	 * @param distance the unweighted distance in pixels
	 * @return the weighted distance
	 */
	public abstract float weightDistance(float distance);
}
