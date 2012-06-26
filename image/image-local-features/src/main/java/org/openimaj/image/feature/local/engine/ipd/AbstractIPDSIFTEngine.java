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
import org.openimaj.image.analysis.pyramid.gaussian.GaussianPyramid;
import org.openimaj.image.analysis.pyramid.gaussian.GaussianPyramidOptions;
import org.openimaj.image.feature.local.descriptor.gradient.SIFTFeatureProvider;
import org.openimaj.image.feature.local.detector.dog.extractor.DominantOrientationExtractor;
import org.openimaj.image.feature.local.detector.ipd.collector.InterestPointFeatureCollector;
import org.openimaj.image.feature.local.detector.ipd.extractor.InterestPointGradientFeatureExtractor;
import org.openimaj.image.feature.local.detector.ipd.finder.OctaveInterestPointFinder;
import org.openimaj.image.feature.local.interest.IPDSelectionMode;
import org.openimaj.image.feature.local.interest.InterestPointData;
import org.openimaj.image.feature.local.interest.InterestPointDetector;
import org.openimaj.image.feature.local.keypoints.InterestPointKeypoint;

/**
 * Extract SIFT features as defined by David Lowe but located using interest point detectors.
 * 
 * This Engine allows the control interest point detector used, whether scale simulation should be used
 * and how interest point patches are extracted.
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @param <T> The type of {@link InterestPointData} 
 */
public abstract class AbstractIPDSIFTEngine<T extends InterestPointData> {
	
	private static final boolean DEFAULT_ACROSS_SCALES = false;
	private static final IPDSelectionMode DEFAULT_SELECTION_MODE = new IPDSelectionMode.Threshold(2500f);
	
	private FinderMode<T> finderMode = new FinderMode.Basic<T>();
	
	private InterestPointDetector<T> detector;
	private boolean acrossScales = DEFAULT_ACROSS_SCALES;
	private IPDSelectionMode selectionMode = DEFAULT_SELECTION_MODE ;
	
	/**
	 * set the selection mode number
	 * @param selectionMode the selection mode
	 */
	public void setSelectionMode(IPDSelectionMode selectionMode) {
		this.selectionMode = selectionMode;
	}
	/**
	 * Initiate the engine with a given detector.
	 * @param detector
	 */
	public AbstractIPDSIFTEngine(InterestPointDetector<T> detector){
		this.detector = detector;
		this.selectionMode = DEFAULT_SELECTION_MODE;
		
	}
	/**
	 * Find the interest points using the provided detector and extract a SIFT descriptor per point.
	 * @param image to extract features from
	 * @return extracted interest point features
	 */
	public LocalFeatureList<InterestPointKeypoint<T>> findFeatures(FImage image) {
		InterestPointFeatureCollector<T> collector = constructCollector(new InterestPointGradientFeatureExtractor(new DominantOrientationExtractor(), new SIFTFeatureProvider()));
		image = image.multiply(255f);
		if(acrossScales ){
			findAcrossScales(image,collector);
		}
		else{
			findInSingleScale(image,collector);
		}
		return collector.getFeatures();
		
	}

	/**
	 * Given an extractor, construct an {@link InterestPointFeatureCollector}
	 * @param extractor
	 * @return the collector
	 */
	public abstract InterestPointFeatureCollector<T> constructCollector(InterestPointGradientFeatureExtractor extractor);
	
	private void findInSingleScale(FImage image, InterestPointFeatureCollector<T> collector) {
		detector.findInterestPoints(image);
		
		List<T> points = this.selectionMode.selectPoints(this.detector);
		for(T point: points){
			collector.foundInterestPoint(image, point);
		}
	}
	private void findAcrossScales(FImage image, InterestPointFeatureCollector<T> collector) {
		OctaveInterestPointFinder<T> finder = constructFinder();
		finder.setOctaveInterestPointListener(collector);
		GaussianPyramidOptions<FImage> options = new GaussianPyramidOptions<FImage>();
		options.setDoubleInitialImage(false);
		options.setInitialSigma(1.0f);
		options.setExtraScaleSteps(0);
		options.setOctaveProcessor(finder);
		GaussianPyramid<FImage> pyr = new GaussianPyramid<FImage>(options);
		pyr.process(image);
		finder.finish();
	}
	
	private OctaveInterestPointFinder<T> constructFinder() {
		return getFinderMode().finder(this.detector,this.selectionMode);
	}
	/**
	 * @param acrossScales
	 */
	public void setAcrossScales(boolean acrossScales) {
		this.acrossScales = acrossScales;
	}
	/**
	 * set the underlying finder
	 * @param finderMode
	 */
	public void setFinderMode(FinderMode<T> finderMode) {
		this.finderMode = finderMode;
	}
	/**
	 * @return the finder used by {@link #findFeatures(FImage)}
	 */
	public FinderMode<T> getFinderMode() {
		return finderMode;
	}
	
}
