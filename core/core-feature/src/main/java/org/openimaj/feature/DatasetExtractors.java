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

public class DatasetExtractors {
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
