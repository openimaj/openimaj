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
package org.openimaj.feature.local.list;

import org.openimaj.feature.local.LocalFeature;
import org.openimaj.io.Writeable;
import org.openimaj.util.list.RandomisableList;

/**
 * An interface defining list of {@link LocalFeature}s.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <T>
 */
public interface LocalFeatureList<T extends LocalFeature<?, ?>> extends RandomisableList<T>, Writeable {
	/** The header used when writing LocalFeatureLists to streams and files */
	public static final byte[] BINARY_HEADER = "KPT".getBytes();

	/**
	 * Get the feature-vector data of the list as a two-dimensional array of
	 * data. The number of rows will equal the number of features in the list,
	 * and the type <Q>must be compatible with the data type of the features
	 * themselves.
	 * 
	 * @param <Q>
	 *            the data type
	 * @param a
	 *            the array to fill
	 * @return the array, filled with the feature-vector data.
	 */
	public <Q> Q[] asDataArray(Q[] a);

	/**
	 * Get the length of the feature-vectors of each local feature if they are
	 * constant.
	 * 
	 * This value is used as instantiate new local features in the case that the
	 * local feature has a constructor that takes a single integer.
	 * 
	 * @return the feature-vector length
	 */
	public int vecLength();

	@Override
	public LocalFeatureList<T> subList(int fromIndex, int toIndex);
}
