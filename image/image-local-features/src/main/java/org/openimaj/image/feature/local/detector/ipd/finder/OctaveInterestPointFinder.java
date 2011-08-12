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
package org.openimaj.image.feature.local.detector.ipd.finder;

import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.feature.local.detector.ipd.collector.InterestPointFeatureCollector;
import org.openimaj.image.feature.local.interest.AbstractStructureTensorIPD.InterestPointData;
import org.openimaj.image.feature.local.interest.IPDSelectionMode;
import org.openimaj.image.feature.local.interest.InterestPointDetector;
import org.openimaj.image.processing.pyramid.OctaveProcessor;
import org.openimaj.image.processing.pyramid.gaussian.GaussianOctave;
import org.openimaj.image.processing.pyramid.gaussian.GaussianPyramid;

/**
 * Finder with a specified detector which finds interest points at a given gaussian octave. This is often
 * used in conjunction with a {@link GaussianPyramid} which provides {@link GaussianOctave} instances. 
 * 
 * This finder calls a specified {@link InterestPointFeatureCollector} which does something with the features
 * located at a given octave.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class OctaveInterestPointFinder implements OctaveProcessor<GaussianOctave<FImage>, FImage> {
	
	private InterestPointDetector detector;
	private InterestPointFeatureCollector listener;
	private IPDSelectionMode selectionMode;

	/**
	 * @param detector the detector with which features are found
	 * @param mode the detector's feature selection mode
	 * @param modeNumber the selection mode's selection condition number
	 */
	public OctaveInterestPointFinder(InterestPointDetector detector, IPDSelectionMode selectionMode) {
		this.detector = detector;
		this.selectionMode = selectionMode;
	}

	@Override
	public void process(GaussianOctave<FImage> octave) {
		for (int currentScaleIndex = 0; currentScaleIndex < octave.images.length; currentScaleIndex++) {
			FImage fImage = octave.images[currentScaleIndex];
			float currentScale = (float) (octave.options.getInitialSigma() * Math.pow(2, currentScaleIndex/octave.options.getScales()));
			detector.setDetectionScaleVariance(currentScale);
			detector.findInterestPoints(fImage);
			List<InterestPointData> points = this.selectionMode.selectPoints(detector);
			for(InterestPointData  point: points){
				this.listener.foundInterestPoint(fImage, point,octave.octaveSize);
			}	
		}
		
	}
	
	

	/**
	 * @param listener to be informed on detection of new interest points
	 */
	public void setOctaveInterestPointListener(InterestPointFeatureCollector listener) {
		this.listener = listener;
	}

}
