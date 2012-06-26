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
package org.openimaj.image.feature.local.detector.ipd.collector;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.image.FImage;
import org.openimaj.image.feature.local.detector.ipd.extractor.InterestPointGradientFeatureExtractor;
import org.openimaj.image.feature.local.interest.InterestPointData;
import org.openimaj.image.feature.local.keypoints.InterestPointKeypoint;

/**
 * An interest point feature collector can be used to hold interest points found in an image.
 * Interest point collectors decide what area of the image is considered "interesting" pass this 
 * information on to their designated extractor.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @param <T> The type of {@link InterestPointData}
 */
public abstract class InterestPointFeatureCollector<T extends InterestPointData> {
	
	protected MemoryLocalFeatureList<InterestPointKeypoint<T>> features;
	protected InterestPointGradientFeatureExtractor extractor;

	/**
	 * Initialise the collector with a memory local feature list types on {@link InterestPointKeypoint}
	 * @param extractor when interest points are found and prepared, inform this extractor
	 */
	public InterestPointFeatureCollector(InterestPointGradientFeatureExtractor extractor){
		this.features = new MemoryLocalFeatureList<InterestPointKeypoint<T>>();
		this.extractor = extractor;
	}
	
	/**
	 * @return the features collected
	 */
	public LocalFeatureList<InterestPointKeypoint<T>> getFeatures() {
		return this.features;
	}
	
	/**
	 * Collect interest points from a single image
	 * @param image
	 * @param point
	 */
	public abstract void foundInterestPoint(FImage image,T point);

	/**
	 * Collect interest points from an image known to be in a pyramid with a given octave size
	 * @param image
	 * @param point
	 * @param octaveSize
	 */
	public abstract void foundInterestPoint(FImage image,T point, double octaveSize);
}
