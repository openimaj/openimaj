///**
// * Copyright (c) 2011, The University of Southampton and the individual contributors.
// * All rights reserved.
// *
// * Redistribution and use in source and binary forms, with or without modification,
// * are permitted provided that the following conditions are met:
// *
// *   * 	Redistributions of source code must retain the above copyright notice,
// * 	this list of conditions and the following disclaimer.
// *
// *   *	Redistributions in binary form must reproduce the above copyright notice,
// * 	this list of conditions and the following disclaimer in the documentation
// * 	and/or other materials provided with the distribution.
// *
// *   *	Neither the name of the University of Southampton nor the names of its
// * 	contributors may be used to endorse or promote products derived from this
// * 	software without specific prior written permission.
// *
// * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
// * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
// * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
// * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
// * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
// */
//package org.openimaj.image.feature.local.engine;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.openimaj.feature.local.list.LocalFeatureList;
//import org.openimaj.image.FImage;
//import org.openimaj.image.MBFImage;
//import org.openimaj.image.colour.ColourSpace;
//import org.openimaj.image.feature.local.descriptor.gradient.SIFTFeatureProvider;
//import org.openimaj.image.feature.local.detector.dog.collector.Collector;
//import org.openimaj.image.feature.local.detector.dog.collector.OctaveKeypointCollector;
//import org.openimaj.image.feature.local.detector.dog.extractor.DominantOrientationExtractor;
//import org.openimaj.image.feature.local.detector.dog.extractor.GradientFeatureExtractor;
//import org.openimaj.image.feature.local.detector.dog.extractor.OrientationHistogramExtractor;
//import org.openimaj.image.feature.local.detector.dog.pyramid.DoGOctaveExtremaFinder;
//import org.openimaj.image.feature.local.detector.pyramid.BasicOctaveExtremaFinder;
//import org.openimaj.image.feature.local.detector.pyramid.OctaveInterestPointFinder;
//import org.openimaj.image.feature.local.keypoints.Keypoint;
//import org.openimaj.image.processing.pyramid.gaussian.GaussianOctave;
//import org.openimaj.image.processing.pyramid.gaussian.GaussianPyramid;
//
///**
// * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
// *
// */
//public class DoGColourSIFTEngine implements Engine<Keypoint, MBFImage> {
//	DoGSIFTEngineOptions options;
//	
//	public DoGColourSIFTEngine() {
//		this(new DoGSIFTEngineOptions());
//	}
//	
//	public DoGColourSIFTEngine(DoGSIFTEngineOptions options) {
//		this.options = options;
//	}
//	
//	@Override
//	public LocalFeatureList<Keypoint> findFeatures(MBFImage image) {
//		FImage luminance = ColourSpace.convert(image, ColourSpace.LUMINANCE_NTSC).bands.get(0);
//		
//		return findFeatures(image, luminance);
//	}
//		
//	protected LocalFeatureList<Keypoint> findInterestPointsAndOrientations(FImage luminance) {
//		OctaveInterestPointFinder<GaussianOctave<FImage>, FImage> finder = 
//			new DoGOctaveExtremaFinder(new BasicOctaveExtremaFinder(options.magnitudeThreshold, options.eigenvalueRatio));
//		
//		Collector<GaussianOctave<FImage>, Keypoint, FImage> collector = new OctaveKeypointCollector(
//				new GradientFeatureExtractor(
//					new DominantOrientationExtractor(
//							options.peakThreshold, 
//							new OrientationHistogramExtractor(
//									options.numOriHistBins, 
//									options.scaling, 
//									options.smoothingIterations, 
//									options.samplingSize
//							)
//					),
//					new SIFTFeatureProvider(
//							options.numOriBins, 
//							options.numSpatialBins, 
//							options.valueThreshold, 
//							options.gaussianSigma
//					), 
//					0 //set the mag factor to zero; this stops any SIFT samples being accumulated
//				)
//		);
//		
//		finder.setOctaveInterestPointListener(collector);
//		
//		options.setOctaveProcessor(finder);
//		
//		GaussianPyramid<FImage> pyr = new GaussianPyramid<FImage>(options);
//		pyr.process(luminance);
//		
//		return collector.getFeatures();
//	}
//	
//	public LocalFeatureList<Keypoint> findFeatures(MBFImage image, FImage luminance) {
//		LocalFeatureList<Keypoint> kpts = findInterestPointsAndOrientations(luminance);
//		
//		final int numBands = image.numBands();
//		List<LocalFeatureList<Keypoint>> bandKpts = new ArrayList<LocalFeatureList<Keypoint>>(numBands);
//		
//		for (int i=0; i<numBands; i++) {
//			bandKpts.add(generateFeaturesForBand(image.bands.get(i)));
//		}
//		
//		for (int j=0; j<kpts.size(); j++) {
//			Keypoint key = kpts.get(j);
//			
//			for (int i=0; i<numBands; i++) {
//				byte[] ivec = bandKpts.get(i).get(j).ivec;
//				
//				System.arraycopy(ivec, 0, key.ivec, i*ivec.length, ivec.length);
//			}
//		}
//		
//		return kpts;
//	}
//	
//	protected LocalFeatureList<Keypoint> generateFeaturesForBand(FImage image) {
//		OctaveInterestPointFinder<GaussianOctave<FImage>, FImage> finder = new DirectedOctaveInterestPointFinder();  
//		
//		Collector<GaussianOctave<FImage>, Keypoint, FImage> collector = new OctaveKeypointCollector(
//				new GradientFeatureExtractor(
//					new DominantOrientationExtractor(
//							options.peakThreshold, 
//							new OrientationHistogramExtractor(
//									options.numOriHistBins, 
//									options.scaling, 
//									options.smoothingIterations, 
//									options.samplingSize
//							)
//					),
//					new SIFTFeatureProvider(
//							options.numOriBins, 
//							options.numSpatialBins, 
//							options.valueThreshold, 
//							options.gaussianSigma
//					), 
//					options.magnificationFactor * options.numSpatialBins
//				)
//		);
//		
//		finder.setOctaveInterestPointListener(collector);
//		
//		options.setOctaveProcessor(finder);
//		
//		GaussianPyramid<FImage> pyr = new GaussianPyramid<FImage>(options);
//		pyr.process(image);
//		
//		return collector.getFeatures();
//	}
//
//	/**
//	 * @return the current options used by the engine
//	 */
//	public DoGSIFTEngineOptions getOptions() {
//		return options;
//	}
//}
