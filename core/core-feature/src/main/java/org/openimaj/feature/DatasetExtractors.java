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
