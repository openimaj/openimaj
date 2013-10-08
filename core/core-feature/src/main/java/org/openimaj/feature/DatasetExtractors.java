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

import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;

import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListBackedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.MapBackedDataset;

/**
 * Static methods to generate datasets of features from datasets of objects
 * using a feature extractor.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class DatasetExtractors {
	private DatasetExtractors() {
	}

	/**
	 * Create a {@link ListDataset} of features from the given
	 * {@link ListDataset} of objects by extracting the features from the
	 * objects with the given feature extractor.
	 * <p>
	 * This function is produces a lazy {@link ListDataset} instance that
	 * extracts features on demand. No caching is performed, so if you
	 * <code>get</code> from the resultant list for the same element many times,
	 * you'll invoke the feature extractor each time.
	 * 
	 * @param input
	 *            the {@link ListDataset} of input objects
	 * @param extractor
	 *            the feature extractor
	 * @return a {@link ListDataset} of features
	 */
	public static <FEATURE, OBJECT> ListDataset<FEATURE> createLazyFeatureDataset(final ListDataset<OBJECT> input,
			final FeatureExtractor<FEATURE, OBJECT> extractor)
	{
		return new ListBackedDataset<FEATURE>(new AbstractList<FEATURE>() {
			@Override
			public FEATURE get(int index) {
				return extractor.extractFeature(input.get(index));
			}

			@Override
			public int size() {
				return input.size();
			}
		});
	}

	/**
	 * Create a {@link GroupedDataset} of keys to {@link ListDataset} of
	 * features from the given {@link GroupedDataset} of keys to
	 * {@link ListDataset}s of objects by extracting the features from the
	 * objects with the given feature extractor.
	 * <p>
	 * This function is produces a lazy {@link GroupedDataset} instance that
	 * extracts features on demand. No caching is performed, so if you
	 * <code>get</code> from the resultant list for the same element many times,
	 * you'll invoke the feature extractor each time.
	 * 
	 * @param input
	 *            the {@link GroupedDataset} of input objects
	 * @param extractor
	 *            the feature extractor
	 * @return a {@link GroupedDataset} of features
	 */
	public static <FEATURE, OBJECT, KEY> GroupedDataset<KEY, ListDataset<FEATURE>, FEATURE> createLazyFeatureDataset(
			final GroupedDataset<KEY, ? extends ListDataset<OBJECT>, OBJECT> input,
			final FeatureExtractor<FEATURE, OBJECT> extractor)
	{
		return new MapBackedDataset<KEY, ListDataset<FEATURE>, FEATURE>(new AbstractMap<KEY, ListDataset<FEATURE>>() {
			@Override
			public int size() {
				return input.size();
			}

			@Override
			public boolean isEmpty() {
				return input.isEmpty();
			}

			@Override
			public boolean containsKey(Object key) {
				return input.containsKey(key);
			}

			@Override
			public ListDataset<FEATURE> get(Object key) {
				return createLazyFeatureDataset(input.get(key), extractor);
			}

			@Override
			public Set<KEY> keySet() {
				return input.keySet();
			}

			@Override
			public Set<java.util.Map.Entry<KEY, ListDataset<FEATURE>>> entrySet() {
				return new AbstractSet<Entry<KEY, ListDataset<FEATURE>>>() {

					@Override
					public Iterator<java.util.Map.Entry<KEY, ListDataset<FEATURE>>> iterator() {
						return new Iterator<Entry<KEY, ListDataset<FEATURE>>>() {
							Iterator<?> internal = input.entrySet().iterator();

							@Override
							public boolean hasNext() {
								return internal.hasNext();
							}

							@Override
							public Entry<KEY, ListDataset<FEATURE>> next() {
								return new Entry<KEY, ListDataset<FEATURE>>() {
									@SuppressWarnings("unchecked")
									Entry<KEY, OBJECT> next = (Entry<KEY, OBJECT>) internal.next();

									@Override
									public KEY getKey() {
										return next.getKey();
									}

									@SuppressWarnings("unchecked")
									@Override
									public ListDataset<FEATURE> getValue() {
										return createLazyFeatureDataset((ListDataset<OBJECT>) next.getValue(), extractor);
									}

									@Override
									public ListDataset<FEATURE> setValue(ListDataset<FEATURE> value) {
										throw new UnsupportedOperationException();
									}
								};
							}

							@Override
							public void remove() {
								throw new UnsupportedOperationException();
							}
						};
					}

					@Override
					public int size() {
						return input.size();
					}
				};
			}

		});
	}
}
