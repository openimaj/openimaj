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
import org.openimaj.citation.annotation.References;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.FImage;
import org.openimaj.image.analysis.pyramid.gaussian.GaussianOctave;
import org.openimaj.image.analysis.pyramid.gaussian.GaussianPyramid;
import org.openimaj.image.feature.local.descriptor.gradient.SIFTFeatureProvider;
import org.openimaj.image.feature.local.detector.dog.collector.Collector;
import org.openimaj.image.feature.local.detector.dog.collector.OctaveKeypointCollector;
import org.openimaj.image.feature.local.detector.dog.extractor.DominantOrientationExtractor;
import org.openimaj.image.feature.local.detector.dog.extractor.GradientFeatureExtractor;
import org.openimaj.image.feature.local.detector.dog.extractor.OrientationHistogramExtractor;
import org.openimaj.image.feature.local.detector.dog.pyramid.DoGOctaveExtremaFinder;
import org.openimaj.image.feature.local.detector.pyramid.BasicOctaveExtremaFinder;
import org.openimaj.image.feature.local.detector.pyramid.OctaveInterestPointFinder;
import org.openimaj.image.feature.local.keypoints.Keypoint;

/**
 * <p>
 * An implementation of Lowe's SIFT: specifically both the
 * difference-of-Gaussian detector coupled with a SIFT descriptor.
 * </p>
 * <p>
 * This class and its sister options class {@link DoGSIFTEngineOptions} wrap all
 * the work needed to extract SIFT features into a single place without having
 * to deal with the setup of pyramid finders, collectors and providers.
 * </p>
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
@References(references = {
		@Reference(
				type = ReferenceType.Article,
				author = { "David Lowe" },
				title = "Distinctive image features from scale-invariant keypoints",
				year = "2004",
				journal = "IJCV",
				pages = { "91", "110" },
				month = "January",
				number = "2",
				volume = "60"),
		@Reference(
				type = ReferenceType.Inproceedings,
				author = { "David Lowe" },
				title = "Object recognition from local scale-invariant features",
				year = "1999",
				booktitle = "Proc. of the International Conference on Computer Vision {ICCV}",
				pages = { "1150", "1157" }
		)
})
public class DoGSIFTEngine implements Engine<Keypoint, FImage> {
	DoGSIFTEngineOptions<FImage> options;

	/**
	 * Construct a DoGSIFTEngine with the default options.
	 */
	public DoGSIFTEngine() {
		this(new DoGSIFTEngineOptions<FImage>());
	}

	/**
	 * Construct a DoGSIFTEngine with the given options.
	 * 
	 * @param options
	 *            the options
	 */
	public DoGSIFTEngine(DoGSIFTEngineOptions<FImage> options) {
		this.options = options;
	}

	@Override
	public LocalFeatureList<Keypoint> findFeatures(FImage image) {
		final OctaveInterestPointFinder<GaussianOctave<FImage>, FImage> finder =
				new DoGOctaveExtremaFinder(new BasicOctaveExtremaFinder(options.magnitudeThreshold,
						options.eigenvalueRatio));

		final Collector<GaussianOctave<FImage>, Keypoint, FImage> collector = new OctaveKeypointCollector<FImage>(
				new GradientFeatureExtractor(
						new DominantOrientationExtractor(
								options.peakThreshold,
								new OrientationHistogramExtractor(
										options.numOriHistBins,
										options.scaling,
										options.smoothingIterations,
										options.samplingSize
								)
						),
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

	/**
	 * @return the current options used by the engine
	 */
	public DoGSIFTEngineOptions<FImage> getOptions() {
		return options;
	}
}
