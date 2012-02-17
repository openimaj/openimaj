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

import org.openimaj.feature.local.LocalFeature;
import org.openimaj.image.FImage;
import org.openimaj.image.analysis.pyramid.gaussian.GaussianOctave;
import org.openimaj.image.analysis.pyramid.gaussian.GaussianPyramidOptions;
import org.openimaj.image.feature.local.detector.dog.collector.Collector;
import org.openimaj.image.feature.local.detector.pyramid.OctaveInterestPointFinder;

public class FImagePyramidEngineOptions<FEATURE extends LocalFeature<?>> extends GaussianPyramidOptions<FImage> {
	protected OctaveInterestPointFinder<GaussianOctave<FImage>, FImage> finder;
	protected Collector<GaussianOctave<FImage>, FEATURE, FImage> collector;
	
	/**
	 * @return the finder
	 */
	public OctaveInterestPointFinder<GaussianOctave<FImage>, FImage> getFinder() {
		return finder;
	}
	
	/**
	 * @param finder the finder to set
	 */
	public void setFinder(
			OctaveInterestPointFinder<GaussianOctave<FImage>, FImage> finder) {
		this.finder = finder;
	}
	
	/**
	 * @return the collector
	 */
	public Collector<GaussianOctave<FImage>, FEATURE, FImage> getCollector() {
		return collector;
	}
	
	/**
	 * @param collector the collector to set
	 */
	public void setCollector(
			Collector<GaussianOctave<FImage>, FEATURE, FImage> collector) {
		this.collector = collector;
	}
}