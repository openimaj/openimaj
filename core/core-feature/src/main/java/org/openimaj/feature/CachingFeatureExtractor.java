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
package org.openimaj.feature;

import java.util.HashMap;

import org.openimaj.data.identity.Identifiable;

/**
 * A simple wrapper for a feature extractor that caches the extracted feature to
 * a {@link HashMap}. If a feature has already been generated for a given
 * object, it will be re-read from the {@link HashMap}
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <FEATURE>
 *            Type of feature
 * @param <OBJECT>
 *            Type of object
 */
public class CachingFeatureExtractor<FEATURE, OBJECT extends Identifiable>
		implements
		FeatureExtractor<FEATURE, OBJECT>
{
	private FeatureExtractor<FEATURE, OBJECT> extractor;
	private boolean force;

	private HashMap<String, FEATURE> cache;

	/**
	 * Construct the cache {@link HashMap}. The given extractor will be used to
	 * generate the features.
	 * 
	 * @param extractor
	 *            the feature extractor
	 */
	public CachingFeatureExtractor(FeatureExtractor<FEATURE, OBJECT> extractor) {
		this(extractor, false);
	}

	/**
	 * Construct the cache {@link HashMap} The given extractor will be used to
	 * generate the features. Optionally, all features can be regenerated.
	 * 
	 * @param extractor
	 *            the feature extractor
	 * @param force
	 *            if true, then all features will be regenerated and saved,
	 *            rather than being loaded.
	 */
	public CachingFeatureExtractor(FeatureExtractor<FEATURE, OBJECT> extractor, boolean force) {
		this.cache = new HashMap<String, FEATURE>();
		this.extractor = extractor;
		this.force = force;
	}

	@Override
	public FEATURE extractFeature(OBJECT object) {
		final FEATURE cachedFeature = this.cache.get(object.getID());

		FEATURE feature = null;
		if (!force && cachedFeature != null) {
			feature = cachedFeature;

			if (feature != null)
				return feature;
		}

		feature = extractor.extractFeature(object);
		this.cache.put(object.getID(), feature);
		return feature;
	}

	@Override
	public String toString() {
		return this.extractor.toString();
	}

}
