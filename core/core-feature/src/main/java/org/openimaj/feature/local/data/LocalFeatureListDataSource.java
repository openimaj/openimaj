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
package org.openimaj.feature.local.data;

import java.util.List;
import java.util.Map;

import org.openimaj.data.AbstractMultiListDataSource;
import org.openimaj.data.DataSource;
import org.openimaj.feature.ArrayFeatureVector;
import org.openimaj.feature.FeatureVector;
import org.openimaj.feature.local.LocalFeature;
import org.openimaj.feature.local.list.LocalFeatureList;

/**
 * A {@link DataSource} for the feature vector of one or more lists of
 * {@link LocalFeature}s that use an {@link ArrayFeatureVector} for the feature
 * vector. This can be used as a convenience when you want to feed multiple
 * lists of local features to a clustering algorithm.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <T>
 *            The type of {@link LocalFeature}
 * @param <F>
 *            The type of {@link FeatureVector}.
 */
public class LocalFeatureListDataSource<T extends LocalFeature<?, ? extends ArrayFeatureVector<F>>, F>
extends
AbstractMultiListDataSource<F, T>
{
	/**
	 * Construct with the given list of data
	 *
	 * @param data
	 *            the data
	 */
	public LocalFeatureListDataSource(LocalFeatureList<T> data) {
		super(data);
	}

	/**
	 * Construct with the given lists of data
	 *
	 * @param data
	 *            the data
	 */
	@SafeVarargs
	public LocalFeatureListDataSource(LocalFeatureList<T>... data) {
		super(data);
	}

	/**
	 * Construct with the given lists of data
	 *
	 * @param data
	 *            the data
	 */
	public LocalFeatureListDataSource(List<LocalFeatureList<T>> data) {
		super(data);
	}

	/**
	 * Construct with the given map of data. The keys are ignored, and only the
	 * values are used.
	 *
	 * @param data
	 *            the data
	 */
	public LocalFeatureListDataSource(Map<?, LocalFeatureList<T>> data) {
		super(data);
	}

	@Override
	public int numDimensions() {
		return ((LocalFeatureList<T>) this.data.get(0)).vecLength();
	}

	@Override
	protected F convert(T ele) {
		return ele.getFeatureVector().values;
	}
}
