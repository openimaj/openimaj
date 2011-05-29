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
package org.openimaj.image.feature.local.engine.ipd;

import java.util.List;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.FImage;
import org.openimaj.image.feature.local.descriptor.gradient.SIFTFeatureProvider;
import org.openimaj.image.feature.local.detector.ipd.collector.AffineInterestPointFeatureCollector;
import org.openimaj.image.feature.local.detector.ipd.collector.CircularInterestPointFeatureCollector;
import org.openimaj.image.feature.local.detector.ipd.collector.InterestPointFeatureCollector;
import org.openimaj.image.feature.local.detector.ipd.extractor.InterestPointGradientFeatureExtractor;
import org.openimaj.image.feature.local.detector.ipd.finder.OctaveInterestPointFinder;
import org.openimaj.image.feature.local.detector.ipd.finder.OctaveInterestPointFinder.FeatureMode;
import org.openimaj.image.feature.local.interest.AbstractIPD.InterestPointData;
import org.openimaj.image.feature.local.interest.InterestPointDetector;
import org.openimaj.image.feature.local.keypoints.InterestPointKeypoint;
import org.openimaj.image.processing.pyramid.gaussian.GaussianPyramid;
import org.openimaj.image.processing.pyramid.gaussian.GaussianPyramidOptions;

/**
 * Extract SIFT features as defined by David Lowe but located using interest point detectors.
 * 
 * This Engine allows the control interest point detector used, whether scale simulation should be used
 * and how interest point patches are extracted.
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class IPDSIFTEngine {
	
	private static final int DEFAULT_NPOINTS = -1;
	private static final CollectorMode DEFAULT_COLLECTOR_MODE = CollectorMode.AFFINE;
	private static final boolean DEFAULT_ACROSS_SCALES = false;


	
	/**
	 * The collector patch mode
	 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
	 *
	 */
	public enum CollectorMode{
		/**
		 * Use an {@link AffineInterestPointFeatureCollector}
		 */
		AFFINE,
		/**
		 * Use an {@link CircularInterestPointFeatureCollector} 
		 */
		CIRCULAR
	}
	
	
	
	private InterestPointDetector detector;
	private double modeNumber = DEFAULT_NPOINTS;


	private FeatureMode mode;
	private CollectorMode collectorMode = DEFAULT_COLLECTOR_MODE;
	private boolean acrossScales = DEFAULT_ACROSS_SCALES;


	/**
	 * The number fed to the selection mode
	 * @return threshold or number
	 */
	public double getThreshold() {
		return modeNumber;
	}
	/**
	 * set the selection mode number
	 * @param modeNumber
	 */
	public void setFeatureModeLevel(double modeNumber) {
		this.modeNumber = modeNumber;
	}
	/**
	 * Initiate the engine with a given detector.
	 * @param detector
	 */
	public IPDSIFTEngine(InterestPointDetector detector){
		this.detector = detector;
		
	}
	/**
	 * Find the interest points using the provided detector and extract a SIFT descriptor per point.
	 * @param image to extract features from
	 * @return extracted interest point features
	 */
	public LocalFeatureList<InterestPointKeypoint> findFeatures(FImage image) {
		InterestPointFeatureCollector collector = null;
		
		switch(this.collectorMode){
			case AFFINE:
				collector = new AffineInterestPointFeatureCollector(new InterestPointGradientFeatureExtractor(new SIFTFeatureProvider()));
				break;
			case CIRCULAR:
				collector = new CircularInterestPointFeatureCollector(new InterestPointGradientFeatureExtractor(new SIFTFeatureProvider()));
				break;
		}
		if(acrossScales ){
			findAcrossScales(image,collector);
		}
		else{
			findInSingleScale(image,collector);
		}
		return collector.getFeatures();
		
	}

	private void findInSingleScale(FImage image, InterestPointFeatureCollector collector) {
		detector.findInterestPoints(image);
		
		List<InterestPointData> points = null;
		switch(this.mode){
		case THRESHOLD:
			points = this.detector.getInterestPoints((float) modeNumber);
			break;
		case NUMBER:
			points = this.detector.getInterestPoints((int) modeNumber);
			break;
		}
		for(InterestPointData  point: points){
			collector.foundInterestPoint(image, point);
		}
	}
	private void findAcrossScales(FImage image, InterestPointFeatureCollector collector) {
		OctaveInterestPointFinder finder = new OctaveInterestPointFinder(this.detector,this.mode,this.modeNumber);
		finder.setOctaveInterestPointListener(collector);
		GaussianPyramidOptions<FImage> options = new GaussianPyramidOptions<FImage>();
		options.setDoubleInitialImage(false);
		options.setScales(2); // This level and the next level
		options.setExtraScaleSteps(0);
		GaussianPyramid<FImage> pyr = new GaussianPyramid<FImage>(options);
		pyr.process(image);
	}
	
	/**
	 * Set the selection mode
	 * @param mode
	 */
	public void setMode(FeatureMode mode) {
		this.mode = mode;
	}
	/**
	 * set the collector mode
	 * @param collectorMode
	 */
	public void setCollectorMode(CollectorMode collectorMode) {
		this.collectorMode = collectorMode;
	}
}
