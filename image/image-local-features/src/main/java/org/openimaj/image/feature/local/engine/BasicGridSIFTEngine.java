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
package org.openimaj.image.feature.local.engine;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.FImage;
import org.openimaj.image.analysis.pyramid.gaussian.GaussianOctave;
import org.openimaj.image.analysis.pyramid.gaussian.GaussianPyramid;
import org.openimaj.image.feature.local.descriptor.gradient.SIFTFeatureProvider;
import org.openimaj.image.feature.local.detector.dog.collector.Collector;
import org.openimaj.image.feature.local.detector.dog.collector.OctaveKeypointCollector;
import org.openimaj.image.feature.local.detector.dog.extractor.AbstractDominantOrientationExtractor;
import org.openimaj.image.feature.local.detector.dog.extractor.DominantOrientationExtractor;
import org.openimaj.image.feature.local.detector.dog.extractor.GradientFeatureExtractor;
import org.openimaj.image.feature.local.detector.dog.extractor.NullOrientationExtractor;
import org.openimaj.image.feature.local.detector.dog.extractor.OrientationHistogramExtractor;
import org.openimaj.image.feature.local.detector.pyramid.BasicOctaveGridFinder;
import org.openimaj.image.feature.local.detector.pyramid.OctaveInterestPointFinder;
import org.openimaj.image.feature.local.keypoints.Keypoint;

/**
 * Really basic SIFT extraction on a regular grid of interest points. This is
 * basically a naive implementation of dense sift. Features can either be
 * oriented or upright.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class BasicGridSIFTEngine implements Engine<Keypoint, FImage> {
	boolean orientate;
	DoGSIFTEngineOptions<FImage> options;

	/**
	 * Default constructor.
	 * 
	 * @param orientate
	 *            if true oriented features will be produced; false means
	 *            upright features.
	 */
	public BasicGridSIFTEngine(boolean orientate) {
		this.options = new DoGSIFTEngineOptions<FImage>();
		this.orientate = orientate;
	}

	/**
	 * Construct with the given parameters.
	 * 
	 * @param orientate
	 *            if true oriented features will be produced; false means
	 *            upright features.
	 * @param options
	 *            options for the SIFT extraction
	 */
	public BasicGridSIFTEngine(boolean orientate, DoGSIFTEngineOptions<FImage> options) {
		this.orientate = orientate;
		this.options = options;
	}

	@Override
	public LocalFeatureList<Keypoint> findFeatures(FImage image) {
		final OctaveInterestPointFinder<GaussianOctave<FImage>, FImage> finder = new BasicOctaveGridFinder<GaussianOctave<FImage>, FImage>();

		final AbstractDominantOrientationExtractor ori = orientate ?
				new DominantOrientationExtractor(
						options.peakThreshold,
						new OrientationHistogramExtractor(
								options.numOriHistBins,
								options.scaling,
								options.smoothingIterations,
								options.samplingSize
						)
				)
				: new NullOrientationExtractor();

		final Collector<GaussianOctave<FImage>, Keypoint, FImage> collector = new OctaveKeypointCollector<FImage>(
				new GradientFeatureExtractor(
						ori,
						new SIFTFeatureProvider(
								options.numOriBins,
								options.numSpatialBins,
								options.valueThreshold,
								options.gaussianSigma
						),
						options.magnificationFactor * options.numSpatialBins
				)
				);

		finder.setOctaveInterestPointListener(collector);

		options.setOctaveProcessor(finder);

		final GaussianPyramid<FImage> pyr = new GaussianPyramid<FImage>(options);
		pyr.process(image);

		return collector.getFeatures();
	}
}
