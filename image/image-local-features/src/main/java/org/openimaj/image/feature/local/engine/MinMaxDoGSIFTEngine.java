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

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.FImage;
import org.openimaj.image.analysis.pyramid.gaussian.GaussianOctave;
import org.openimaj.image.analysis.pyramid.gaussian.GaussianPyramid;
import org.openimaj.image.feature.local.descriptor.gradient.SIFTFeatureProvider;
import org.openimaj.image.feature.local.detector.dog.collector.Collector;
import org.openimaj.image.feature.local.detector.dog.collector.OctaveMinMaxKeypointCollector;
import org.openimaj.image.feature.local.detector.dog.extractor.DominantOrientationExtractor;
import org.openimaj.image.feature.local.detector.dog.extractor.GradientFeatureExtractor;
import org.openimaj.image.feature.local.detector.dog.extractor.OrientationHistogramExtractor;
import org.openimaj.image.feature.local.detector.dog.pyramid.DoGOctaveExtremaFinder;
import org.openimaj.image.feature.local.detector.pyramid.BasicOctaveExtremaFinder;
import org.openimaj.image.feature.local.detector.pyramid.OctaveInterestPointFinder;
import org.openimaj.image.feature.local.keypoints.MinMaxKeypoint;

/**
 * A modified implementation of Lowe's difference-of-Gaussian detector and SIFT
 * feature extraction technique that also records whether features are detected
 * at local minima or maxima by looking at the sign of the difference of
 * Gaussian. This information can then be used for enhancing matching or
 * clustering.
 * <p>
 * Internally, this class is identical to {@link DoGSIFTEngine}, but uses a
 * {@link OctaveMinMaxKeypointCollector} instead of an
 * {@link OctaveKeypointCollector}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Reference(
		type = ReferenceType.Inproceedings,
		author = { "Jonathon Hare", "Sina Samangooei", "Paul Lewis" },
		title = "Efficient clustering and quantisation of SIFT features: Exploiting characteristics of the SIFT descriptor and interest region detectors under image inversion",
		year = "2011",
		booktitle = "The ACM International Conference on Multimedia Retrieval (ICMR 2011)",
		month = "April",
		publisher = "ACM Press")
public class MinMaxDoGSIFTEngine implements Engine<MinMaxKeypoint, FImage> {
	DoGSIFTEngineOptions<FImage> options;

	/**
	 * Construct a {@link MinMaxDoGSIFTEngine} with the default options.
	 */
	public MinMaxDoGSIFTEngine() {
		this(new DoGSIFTEngineOptions<FImage>());
	}

	/**
	 * Construct a {@link MinMaxDoGSIFTEngine} with the given options.
	 * 
	 * @param options
	 *            the options
	 */
	public MinMaxDoGSIFTEngine(DoGSIFTEngineOptions<FImage> options) {
		this.options = options;
	}

	@Override
	public LocalFeatureList<MinMaxKeypoint> findFeatures(FImage image) {
		final OctaveInterestPointFinder<GaussianOctave<FImage>, FImage> finder =
				new DoGOctaveExtremaFinder(new BasicOctaveExtremaFinder(options.magnitudeThreshold,
						options.eigenvalueRatio));

		final Collector<GaussianOctave<FImage>, MinMaxKeypoint, FImage> collector = new OctaveMinMaxKeypointCollector(
				new GradientFeatureExtractor(
						new DominantOrientationExtractor(options.peakThreshold,
								new OrientationHistogramExtractor(options.numOriHistBins, options.scaling,
										options.smoothingIterations, options.samplingSize)),
						new SIFTFeatureProvider(options.numOriBins, options.numSpatialBins, options.valueThreshold,
								options.gaussianSigma),
						options.magnificationFactor * options.numSpatialBins
				));

		finder.setOctaveInterestPointListener(collector);

		options.setOctaveProcessor(finder);

		final GaussianPyramid<FImage> pyr = new GaussianPyramid<FImage>(options);
		pyr.process(image);

		return collector.getFeatures();
	}

	/**
	 * Get the options for this engine.
	 * 
	 * @return the options for this engine
	 */
	public DoGSIFTEngineOptions<FImage> getOptions() {
		return options;
	}
}
