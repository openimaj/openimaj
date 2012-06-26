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
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class DoGColourSIFTEngine implements Engine<Keypoint, MBFImage> {
	DoGSIFTEngineOptions<MBFImage> options;
	
	public DoGColourSIFTEngine() {
		this(new DoGSIFTEngineOptions<MBFImage>());
	}
	
	public DoGColourSIFTEngine(DoGSIFTEngineOptions<MBFImage> options) {
		this.options = options;
	}
	
	@Override
	public LocalFeatureList<Keypoint> findFeatures(MBFImage image) {
		FImage luminance = ColourSpace.convert(image, ColourSpace.LUMINANCE_NTSC).bands.get(0);
		
		return findFeatures(image, luminance);
	}
	
	public LocalFeatureList<Keypoint> findFeatures(MBFImage image, FImage luminance) {
		MBFImage newimage = new MBFImage(ColourSpace.CUSTOM);
		newimage.bands.add(luminance);
		newimage.bands.addAll(image.bands);
		
		return findFeaturesInternal(newimage);
	}
	
	protected LocalFeatureList<Keypoint> findFeaturesInternal(MBFImage image) {
		OctaveInterestPointFinder<GaussianOctave<MBFImage>, MBFImage> finder = 
			new FirstBandDoGOctaveExtremaFinder(new BasicOctaveExtremaFinder(options.magnitudeThreshold, options.eigenvalueRatio));
		
		Collector<GaussianOctave<MBFImage>, Keypoint, MBFImage> collector = new OctaveKeypointCollector<MBFImage>(
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
		
		GaussianPyramid<MBFImage> pyr = new GaussianPyramid<MBFImage>(options);
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
