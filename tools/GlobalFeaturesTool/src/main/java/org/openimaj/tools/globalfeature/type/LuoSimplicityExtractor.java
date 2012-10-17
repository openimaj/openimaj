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
package org.openimaj.tools.globalfeature.type;

import org.kohsuke.args4j.Option;
import org.openimaj.feature.FeatureVector;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.feature.global.LuoSimplicity;
import org.openimaj.tools.globalfeature.GlobalFeatureExtractor;

/**
 * Luo's simplicity feature
 * @see LuoSimplicity
 */
public class LuoSimplicityExtractor extends GlobalFeatureExtractor {
	@Option(name="--bins-per-band", aliases="-bpb", required=false, usage="Number of bins to split the R, G and B bands into when constructing the histogram (default 16)")
	int binsPerBand = 16;

	@Option(name="--gamma", required=false, usage="percentage threshold on the max value of the histogram for counting high-valued bins (default 0.01)")
	float gamma = 0.01f;

	@Option(name="--alpha", required=false, usage="alpha parameter for determining bounding box size based on the energy ratio (default 0.9)")
	float alpha = 0.9f;

	@Option(name="--max-kernel-size", required=false, usage="maximum smoothing kernel size (default 50)")
	int maxKernelSize;

	@Option(name="--kernel-size-step", required=false, usage="step size to increment smoothing kernel by (default 1)")
	int kernelSizeStep = 1;

	@Option(name="--num-bins", required=false, usage="number of bins for the gradiant histograms (default 41)")
	int nbins = 41;

	@Option(name="--window-size", required=false, usage="window size for estimating depth of field (default 3)")
	int windowSize = 3;

	@Override
	public FeatureVector extract(MBFImage image, FImage mask) {
		LuoSimplicity cc = new LuoSimplicity(binsPerBand, gamma, alpha, maxKernelSize, kernelSizeStep, nbins, windowSize);
		image.analyseWith(cc);
		return cc.getFeatureVector();
	}
}
