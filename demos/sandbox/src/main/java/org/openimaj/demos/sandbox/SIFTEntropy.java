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
package org.openimaj.demos.sandbox;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openimaj.feature.local.filter.ByteEntropyFilter;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.matcher.FastEuclideanKeypointMatcher;
import org.openimaj.feature.local.matcher.MatchingUtilities;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.util.filter.FilterUtils;

public class SIFTEntropy {
	public static void main(String[] args) throws IOException {
		final FImage image1 = ImageUtilities.readF(new File("/Users/jsh2/Data/ukbench/full/ukbench00000.jpg"));
		final FImage image2 = ImageUtilities.readF(new File("/Users/jsh2/Data/ukbench/full/ukbench00001.jpg"));

		final DoGSIFTEngine engine = new DoGSIFTEngine();

		final LocalFeatureList<Keypoint> keys1 = engine.findFeatures(ResizeProcessor.resizeMax(image1, 150));
		final LocalFeatureList<Keypoint> keys2 = engine.findFeatures(ResizeProcessor.resizeMax(image2, 150));

		final List<Keypoint> keys1f = FilterUtils.filter(keys1, new ByteEntropyFilter());
		final List<Keypoint> keys2f = FilterUtils.filter(keys2, new ByteEntropyFilter());

		System.out.println(keys1.size() + " " + keys1f.size());
		System.out.println(keys2.size() + " " + keys2f.size());

		final FastEuclideanKeypointMatcher<Keypoint> matcher = new FastEuclideanKeypointMatcher<Keypoint>(8000);
		matcher.setModelFeatures(keys1);
		matcher.findMatches(keys2);
		System.out.println(matcher.getMatches().size());
		DisplayUtilities.display(MatchingUtilities.drawMatches(image1, image2, matcher.getMatches(), 1F));

		matcher.setModelFeatures(keys1f);
		matcher.findMatches(keys2f);
		System.out.println(matcher.getMatches().size());
		DisplayUtilities.display(MatchingUtilities.drawMatches(image1, image2, matcher.getMatches(), 1F));
	}
}
