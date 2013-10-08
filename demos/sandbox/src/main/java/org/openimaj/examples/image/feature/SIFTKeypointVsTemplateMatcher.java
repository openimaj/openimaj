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
package org.openimaj.examples.image.feature;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.matcher.FastBasicKeypointMatcher;
import org.openimaj.feature.local.matcher.MatchingUtilities;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.analysis.algorithm.TemplateMatcher;
import org.openimaj.image.analysis.algorithm.TemplateMatcher.Mode;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.feature.local.keypoints.KeypointVisualizer;
import org.openimaj.image.pixel.FValuePixel;
import org.openimaj.image.processing.edges.CannyEdgeDetector;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.filter.FilterUtils;
import org.openimaj.util.function.Predicate;


public class SIFTKeypointVsTemplateMatcher {


	public static void main(String[] args) throws MalformedURLException, IOException {

		trySift();
		tryTemplate();
	}

	private static void tryTemplate() throws MalformedURLException, IOException {
		MBFImage template ;
		MBFImage search ;
		FImage templateF;
		FImage searchF;
		template = ImageUtilities.readMBFAlpha(new URL("http://transfer.vidispine.com/540/6c67bb0a27f14e5bdf72fa48eb364/label.png"));
		search = ImageUtilities.readMBF(new URL("http://transfer.vidispine.com/603/d68e56e5dd8d77bf35aea67bc738c/output.jpg"));
		templateF = template.flatten();
		searchF = search.flatten();
		// Resizer so the searching is done on a smaller image
		float ratio = 0.65f;
		ResizeProcessor proc = new ResizeProcessor(ratio);
		// Set up a template matcher with an edge detected tempalte
		TemplateMatcher matcher = new TemplateMatcher(
			templateF.process(proc).process(new CannyEdgeDetector()),
			Mode.NORM_SUM_SQUARED_DIFFERENCE
		);
		// Analyse an edge detected search space
		matcher.analyseImage(
			searchF.process(proc).process(new CannyEdgeDetector())
		);
		FValuePixel[] bestResponses = matcher.getBestResponses(2);
		MBFImage searchDraw = search.clone();
		// Draw the best 2 matches
		for (FValuePixel fValuePixel : bestResponses) {
			searchDraw.drawShape(
					new Rectangle(
							(fValuePixel.x/ratio)-(templateF.width/2),
							(fValuePixel.y/ratio)-(templateF.height/2),
							templateF.width,
							templateF.height
					), RGBColour.RED);
		}
		DisplayUtilities.display(searchDraw);
	}

	private static void trySift() throws IOException, MalformedURLException {
		final MBFImage template ;
		MBFImage search ;
		FImage templateF;
		FImage searchF;
		template = ImageUtilities.readMBFAlpha(new URL("http://transfer.vidispine.com/540/6c67bb0a27f14e5bdf72fa48eb364/label.png"));
		search = ImageUtilities.readMBF(new URL("http://transfer.vidispine.com/603/d68e56e5dd8d77bf35aea67bc738c/output.jpg"));
		templateF = template.flatten();
		searchF = search.flatten();
		DoGSIFTEngine eng = new DoGSIFTEngine();
		List<Keypoint> templateKey = eng.findFeatures(templateF);
		templateKey = FilterUtils.filter(templateKey, new Predicate<Keypoint>(){

			@Override
			public boolean test(Keypoint k) {
				return template.getBand(3).pixels[(int)k.y][(int)k.x] != 0;
			}


		});
		LocalFeatureList<Keypoint> searchKey = eng.findFeatures(searchF);

		FastBasicKeypointMatcher<Keypoint> match = new FastBasicKeypointMatcher<Keypoint>(8);
		match.setModelFeatures(templateKey);
		match.findMatches(searchKey);

		MBFImage matches = MatchingUtilities.drawMatches(template, search, match.getMatches(), RGBColour.RED);

		DisplayUtilities.display(KeypointVisualizer.drawPatchesInplace(template, templateKey, RGBColour.GREEN, RGBColour.RED));
		DisplayUtilities.display(KeypointVisualizer.drawPatchesInplace(search, searchKey, RGBColour.GREEN, RGBColour.RED));
		DisplayUtilities.display(matches);
	}
}
