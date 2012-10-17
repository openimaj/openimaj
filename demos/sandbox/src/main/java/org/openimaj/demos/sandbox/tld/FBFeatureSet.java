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
package org.openimaj.demos.sandbox.tld;

import org.openimaj.video.tracking.klt.Feature;

/**
 * A forward-backward feature. This is a holds 
 * how well a given feature tracks both in a forward backward sense and
 * in a local neighbourhood normalised cross correlation sense. Also
 * the 3 features which were used to calculate these values are held
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class FBFeatureSet{
	
	/**
	 * The feature in the "current" image. i.e. the feature which we are tracking from
	 */
	public Feature start;
	/**
	 * The feature in the "next" image. i.e. the feature we are tracking to.
	 */
	public Feature middle;
	/**
	 * The feature in the "current" image as tracked back from the "next" image
	 */
	public Feature end;
	/**
	 * initialise an FBFeature from a normal feature
	 * @param start @see {@link #start}
	 * @param middle @see {@link #middle} 
	 * @param end @see {@link #end}
	 */
	public FBFeatureSet(Feature start, Feature middle, Feature end) {
		this.start = start;
		this.middle = middle;
		this.end = end;
	}
	
	/**
	 * @see Feature#Feature()
	 */
	public FBFeatureSet() {
		super();
	}
	/**
	 * The forward-backward distance. 
	 * This is the euclidian distance of this feature as tracked from the current image to the next and back to the current.
	 * Lower numbers imply a better feature
	 */
	public float forwardBackDistance;
	/**
	 * How well the local neighbourhood of the feature correlates between the current and next image
	 */
	public float normalisedCrossCorrelation;
}