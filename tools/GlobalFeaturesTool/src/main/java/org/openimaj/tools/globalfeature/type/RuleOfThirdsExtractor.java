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
import org.openimaj.image.feature.global.RuleOfThirds;
import org.openimaj.tools.globalfeature.GlobalFeatureExtractor;

/**
 * Rule of thirds feature
 * @see RuleOfThirds
 */
public class RuleOfThirdsExtractor extends GlobalFeatureExtractor {
	@Option(name="--saliency-sigma", aliases="-sals", required=false, usage="the amount of Gaussian blurring for the saliency estimation (default 1.0)")
	float saliencySigma = 1f;

	@Option(name="--segment-sigma", aliases="-segs", required=false, usage="the amount of Gaussian blurring applied prior to segmentation (default 0.5)")
	float segmenterSigma = 0.5f;

	@Option(name="--threshold", aliases="-k", required=false, usage="the segmentation threshold (default 500/255)")
	float k = 500f / 255f;

	@Option(name="--min-size", aliases="-m", required=false, usage="the minimum segment size (default 50)")
	int minSize = 50;

	@Override
	public FeatureVector extract(MBFImage image, FImage mask) {
		RuleOfThirds cc = new RuleOfThirds(saliencySigma, segmenterSigma, k, minSize);
		image.analyseWith(cc);
		return cc.getFeatureVector();
	}
}
