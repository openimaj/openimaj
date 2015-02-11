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

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.openimaj.image.FImage;
import org.openimaj.image.analysis.pyramid.OctaveProcessor;
import org.openimaj.image.analysis.pyramid.gaussian.GaussianOctave;
import org.openimaj.image.analysis.pyramid.gaussian.GaussianPyramid;
import org.openimaj.image.feature.local.detector.ipd.collector.InterestPointFeatureCollector;
import org.openimaj.image.feature.local.interest.IPDSelectionMode;
import org.openimaj.image.feature.local.interest.InterestPointData;
import org.openimaj.image.feature.local.interest.InterestPointDetector;

/**
 * Finder with a specified detector which finds interest points at a given
 * gaussian octave. This is often used in conjunction with a
 * {@link GaussianPyramid} which provides {@link GaussianOctave} instances.
 *
 * This finder calls a specified {@link InterestPointFeatureCollector} which
 * does something with the features located at a given octave.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @param <T>
 *            The type of {@link InterestPointData}
 *
 */
public class OctaveInterestPointFinder<T extends InterestPointData>
		implements
			OctaveProcessor<GaussianOctave<FImage>, FImage>
{

	protected InterestPointDetector<T> detector;
	protected InterestPointFeatureCollector<T> listener;
	protected IPDSelectionMode selectionMode;
	static Logger logger = Logger.getLogger(OctaveInterestPointFinder.class);
	static {
		BasicConfigurator.configure();
	}

	/**
	 * @param detector
	 *            the detector with which features are found
	 * @param selectionMode
	 *            the detector's feature selection mode
	 */
	public OctaveInterestPointFinder(InterestPointDetector<T> detector, IPDSelectionMode selectionMode) {
		this.detector = detector;
		this.selectionMode = selectionMode;
	}

	@Override
	public void process(GaussianOctave<FImage> octave) {
		for (int currentScaleIndex = 0; currentScaleIndex < octave.images.length; currentScaleIndex++) {
			final FImage fImage = octave.images[currentScaleIndex];
			final float currentScale = (float) (octave.options.getInitialSigma() * Math.pow(2, (float) currentScaleIndex
					/ octave.options.getScales()));
			detector.setDetectionScale(currentScale);
			detector.findInterestPoints(fImage);
			final List<T> points = this.selectionMode.selectPoints(detector);
			processOctaveLevelPoints(fImage, points, currentScale, octave.octaveSize);
		}

	}

	protected void processOctaveLevelPoints(FImage fImage, List<T> points, float currentScale, float octaveSize) {
		logger.info(String.format("At octave scale %4.2f (absolute scale %4.2f) %d points detected", currentScale,
				currentScale * octaveSize, points.size()));
		for (final T point : points) {
			this.listener.foundInterestPoint(fImage, point, octaveSize);
		}
	}

	/**
	 * @param listener
	 *            to be informed on detection of new interest points
	 */
	public void setOctaveInterestPointListener(InterestPointFeatureCollector<T> listener) {
		this.listener = listener;
	}

	/**
	 * Once all the features have been detected, do something (default: nothing)
	 */
	public void finish() {
	}

}
