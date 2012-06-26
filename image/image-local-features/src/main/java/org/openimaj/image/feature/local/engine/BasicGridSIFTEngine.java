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

import java.io.IOException;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.analysis.pyramid.gaussian.GaussianOctave;
import org.openimaj.image.analysis.pyramid.gaussian.GaussianPyramid;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.feature.local.descriptor.gradient.SIFTFeatureProvider;
import org.openimaj.image.feature.local.detector.dog.collector.Collector;
import org.openimaj.image.feature.local.detector.dog.collector.OctaveKeypointCollector;
import org.openimaj.image.feature.local.detector.dog.extractor.GradientFeatureExtractor;
import org.openimaj.image.feature.local.detector.dog.extractor.NullOrientationExtractor;
import org.openimaj.image.feature.local.detector.pyramid.BasicOctaveGridFinder;
import org.openimaj.image.feature.local.detector.pyramid.OctaveInterestPointFinder;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.feature.local.keypoints.KeypointVisualizer;

public class BasicGridSIFTEngine implements Engine<Keypoint, FImage> {
	DoGSIFTEngineOptions<FImage> options;
	
	public BasicGridSIFTEngine() {
		options = new DoGSIFTEngineOptions<FImage>();
	}
	
	@Override
	public LocalFeatureList<Keypoint> findFeatures(FImage image) {
		OctaveInterestPointFinder<GaussianOctave<FImage>, FImage> finder = new BasicOctaveGridFinder<GaussianOctave<FImage>, FImage>();
		
		Collector<GaussianOctave<FImage>, Keypoint, FImage> collector = new OctaveKeypointCollector<FImage>(
				new GradientFeatureExtractor(
//					new DominantOrientationExtractor(
//							options.peakThreshold, 
//							new OrientationHistogramExtractor(
//									options.numOriHistBins, 
//									options.scaling, 
//									options.smoothingIterations, 
//									options.samplingSize
//							)
//					),
					new NullOrientationExtractor(),
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
		
		GaussianPyramid<FImage> pyr = new GaussianPyramid<FImage>(options);
		pyr.process(image);
		
		return collector.getFeatures();
	}
	
	public static void main(String[] args) throws IOException {
		FImage image = ImageUtilities.readF(BasicOctaveGridFinder.class.getResourceAsStream("/org/openimaj/OpenIMAJ.png"));
		MBFImage cimg = new MBFImage(image.clone(), image.clone(), image.clone());
		
		BasicGridSIFTEngine engine = new BasicGridSIFTEngine();
		
		LocalFeatureList<Keypoint> features = engine.findFeatures(image);
		
		KeypointVisualizer.drawPatchesInplace(cimg, features, RGBColour.RED, null);
		DisplayUtilities.display(cimg);
	}
}
