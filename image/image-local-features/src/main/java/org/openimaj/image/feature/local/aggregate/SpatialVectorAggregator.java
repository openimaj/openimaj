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
package org.openimaj.image.feature.local.aggregate;

import java.util.List;

import org.openimaj.feature.FeatureVector;
import org.openimaj.feature.local.LocalFeature;
import org.openimaj.feature.local.Location;

/**
 * Interface describing an object that can convert a list of local features from
 * a single image into an aggregated vector form, using both the featurevector
 * and spatial location of each local feature. An example use is to create
 * aggregate feature vectors that encode go beyond simple
 * {@link VectorAggregator}s in that they additionally encode spatial
 * information.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <FEATURE>
 *            The type of {@link FeatureVector} of the {@link LocalFeature}s
 *            that can be processed.
 * @param <LOCATION>
 *            The type of {@link Location} of the {@link LocalFeature}s that can
 *            be processed.
 * @param <BOUNDS>
 *            The spatial bounds in which the {@link LocalFeature}s were
 *            extracted.
 */
public interface SpatialVectorAggregator<FEATURE extends FeatureVector, LOCATION extends Location, BOUNDS> {
	/**
	 * Aggregate the given features into a vector. The features are assumed to
	 * have spatial locations within the given bounds; typically this might be
	 * the bounds rectangle of the image from which the features were extracted.
	 * 
	 * @param features
	 *            the features to aggregate
	 * @param bounds
	 *            the bounds in which the features were extracted
	 * @return the aggregated vector
	 */
	public FeatureVector aggregate(List<? extends LocalFeature<? extends LOCATION, ? extends FEATURE>> features,
			BOUNDS bounds);
}
