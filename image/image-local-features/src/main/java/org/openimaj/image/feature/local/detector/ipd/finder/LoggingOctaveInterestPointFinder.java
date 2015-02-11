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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openimaj.image.FImage;
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
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @param <T>
 *            The type of {@link InterestPointData}
 *
 */
public class LoggingOctaveInterestPointFinder<T extends InterestPointData> extends OctaveInterestPointFinder<T> {

	private HashMap<String, Integer> scalePoints;
	private ArrayList<String> scales;

	/**
	 * instantiate scalepoints the superclass and scalepoints to log
	 * 
	 * @param detector
	 * @param selectionMode
	 */
	public LoggingOctaveInterestPointFinder(InterestPointDetector<T> detector, IPDSelectionMode selectionMode) {
		super(detector, selectionMode);
		scalePoints = new HashMap<String, Integer>();
		scales = new ArrayList<String>();
	}

	@Override
	protected void processOctaveLevelPoints(FImage fImage, List<T> points, float currentScale, float octaveSize) {
		super.processOctaveLevelPoints(fImage, points, currentScale, octaveSize);
		final String key = String.format("%f-%f", currentScale * octaveSize, octaveSize);
		this.scalePoints.put(key, points.size());
		this.scales.add(key);
	}

	private void printScalePoints() {
		for (final String scale : scales) {
			final Integer entry = this.scalePoints.get(scale);
			System.out.format("%s,%d\n", scale, entry);
		}
	}

	@Override
	public void finish() {
		printScalePoints();
	}
}
