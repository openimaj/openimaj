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
import org.openimaj.image.MBFImage;
import org.openimaj.image.analysis.pyramid.gaussian.GaussianOctave;
import org.openimaj.image.analysis.pyramid.gaussian.GaussianPyramid;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.feature.local.descriptor.gradient.SIFTFeatureProvider;
import org.openimaj.image.feature.local.detector.dog.collector.Collector;
import org.openimaj.image.feature.local.detector.dog.collector.OctaveKeypointCollector;
import org.openimaj.image.feature.local.detector.dog.extractor.ColourGradientFeatureExtractor;
import org.openimaj.image.feature.local.detector.dog.extractor.DominantOrientationExtractor;
import org.openimaj.image.feature.local.detector.dog.extractor.OrientationHistogramExtractor;
import org.openimaj.image.feature.local.detector.dog.pyramid.FirstBandDoGOctaveExtremaFinder;
import org.openimaj.image.feature.local.detector.pyramid.BasicOctaveExtremaFinder;
import org.openimaj.image.feature.local.detector.pyramid.OctaveInterestPointFinder;
import org.openimaj.image.feature.local.keypoints.Keypoint;

/**
 * The {@link DoGSIFTEngine} extended to colour images (aka Colour-SIFT).
 * <p>
 * The {@link DoGColourSIFTEngine} creates a luminance image from which to apply
 * the difference-of-Gaussian interest point detection algorithm, but extracts
 * the actual SIFT features from the bands of the input image directly. This
 * means that the type of Colour-SIFT feature is controlled directly by the
 * colour-space of the input image; for example if an RGB image is given as
 * input, then the feature will be standard RGB-SIFT.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@References(references = {
		@Reference(
				type = ReferenceType.Article,
				author = { "Burghouts, Gertjan J.", "Geusebroek, Jan-Mark" },
				title = "Performance evaluation of local colour invariants",
				year = "2009",
				journal = "Comput. Vis. Image Underst.",
				pages = { "48", "", "62" },
				url = "http://dx.doi.org/10.1016/j.cviu.2008.07.003",
				month = "jan",
				number = "1",
				publisher = "Elsevier Science Inc.",
				volume = "113",
				customData = {
						"issn", "1077-3142",
						"numpages", "15",
						"doi", "10.1016/j.cviu.2008.07.003",
						"acmid", "1465842",
						"address", "New York, NY, USA",
						"keywords", "Colour, Local descriptors, SIFT"
				}
		),
		@Reference(
				type = ReferenceType.Article,
				author = { "van de Sande, K. E. A.", "Gevers, T.", "Snoek, C. G. M." },
				title = "Evaluating Color Descriptors for Object and Scene Recognition",
				year = "2010",
				journal = "IEEE Transactions on Pattern Analysis and Machine Intelligence",
				pages = { "1582", "", "1596" },
				url = "http://www.science.uva.nl/research/publications/2010/vandeSandeTPAMI2010",
				number = "9",
				volume = "32"
		)
})
public class DoGColourSIFTEngine implements Engine<Keypoint, MBFImage> {
	DoGSIFTEngineOptions<MBFImage> options;

	/**
	 * Construct with the default values for the {@link DoGSIFTEngineOptions}.
	 */
	public DoGColourSIFTEngine() {
		this(new DoGSIFTEngineOptions<MBFImage>());
	}

	/**
	 * Construct with the given options.
	 * 
	 * @param options
	 *            the options.
	 */
	public DoGColourSIFTEngine(DoGSIFTEngineOptions<MBFImage> options) {
		this.options = options;
	}

	@Override
	public LocalFeatureList<Keypoint> findFeatures(MBFImage image) {
		final FImage luminance = ColourSpace.convert(image, ColourSpace.LUMINANCE_NTSC).bands.get(0);

		return findFeatures(image, luminance);
	}

	/**
	 * Find DoG interest points in the given luminance image, but extract the
	 * SIFT features from the colour image.
	 * 
	 * @param image
	 *            the colour image to extract the SIFT features from
	 * @param luminance
	 *            the luminance image to detect the interest points in
	 * @return the extracted features
	 */
	public LocalFeatureList<Keypoint> findFeatures(MBFImage image, FImage luminance) {
		final MBFImage newimage = new MBFImage(ColourSpace.CUSTOM);
		newimage.bands.add(luminance);
		newimage.bands.addAll(image.bands);

		return findFeaturesInternal(newimage);
	}

	protected LocalFeatureList<Keypoint> findFeaturesInternal(MBFImage image) {
		final OctaveInterestPointFinder<GaussianOctave<MBFImage>, MBFImage> finder =
				new FirstBandDoGOctaveExtremaFinder(new BasicOctaveExtremaFinder(options.magnitudeThreshold,
						options.eigenvalueRatio));

		final Collector<GaussianOctave<MBFImage>, Keypoint, MBFImage> collector = new OctaveKeypointCollector<MBFImage>(
				new ColourGradientFeatureExtractor(
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

		final GaussianPyramid<MBFImage> pyr = new GaussianPyramid<MBFImage>(options);
		pyr.process(image);

		return collector.getFeatures();
	}

	/**
	 * @return the current options used by the engine
	 */
	public DoGSIFTEngineOptions<MBFImage> getOptions() {
		return options;
	}
}
