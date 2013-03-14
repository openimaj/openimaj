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
	 * Construct with the given lists of data
	 * 
	 * @param data
	 *            the data
	 */
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
