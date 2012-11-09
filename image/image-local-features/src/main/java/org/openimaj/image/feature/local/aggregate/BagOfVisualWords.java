package org.openimaj.image.feature.local.aggregate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openimaj.feature.ArrayFeatureVector;
import org.openimaj.feature.SparseIntFV;
import org.openimaj.feature.local.LocalFeature;
import org.openimaj.feature.local.Location;
import org.openimaj.feature.local.quantised.QuantisedLocalFeature;
import org.openimaj.ml.clustering.assignment.HardAssigner;

/**
 * Implementation of an object capable of extracting basic (hard-assignment) Bag
 * of Visual Words (BoVW) representations of an image given a list of local
 * features and an {@link HardAssigner} with an associated codebook.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <T>
 *            Primitive a type of the {@link ArrayFeatureVector}s used by the
 *            {@link LocalFeature}s that will be processed.
 */
public class BagOfVisualWords<T> implements VectorAggregator<ArrayFeatureVector<T>> {
	private HardAssigner<T, ?, ?> assigner;

	/**
	 * Construct with the given assigner.
	 * 
	 * @param assigner
	 *            the assigner
	 */
	public BagOfVisualWords(HardAssigner<T, ?, ?> assigner) {
		this.assigner = assigner;
	}

	/**
	 * Utility method to construct a list of quantised local features (local
	 * features with visual word assignments) from a list of features and a
	 * {@link HardAssigner}.
	 * 
	 * @param assigner
	 *            the assigner to apply to the feature vectors to create the
	 *            visual word identifiers
	 * @param features
	 *            the features to process
	 * @return a list of features with visual word assignments
	 */
	public static <L extends Location, T> List<QuantisedLocalFeature<L>> computeQuantisedFeatures(
			HardAssigner<T, ?, ?> assigner,
			List<LocalFeature<L, ? extends ArrayFeatureVector<T>>> features)
	{
		final List<QuantisedLocalFeature<L>> out = new ArrayList<QuantisedLocalFeature<L>>(features.size());

		for (final LocalFeature<L, ? extends ArrayFeatureVector<T>> f : features) {
			final int idx = assigner.assign(f.getFeatureVector().values);
			out.add(new QuantisedLocalFeature<L>(f.getLocation(), idx));
		}

		return out;

	}

	/**
	 * Utility method to quickly convert a collection of quantised local
	 * features to a histogram of their corresponding visual word identifiers.
	 * 
	 * @param qfeatures
	 *            the quantised features.
	 * @return a histogram of the occurrences of the visual words
	 */
	public static <L extends Location>
			SparseIntFV
			extractFeatureFromQuantised(Collection<QuantisedLocalFeature<L>> qfeatures)
	{
		final SparseIntFV fv = new SparseIntFV();

		for (final QuantisedLocalFeature<L> qf : qfeatures) {
			fv.values.increment(qf.id, 1);
		}

		return fv;
	}

	@Override
	public SparseIntFV aggregate(List<? extends LocalFeature<?, ? extends ArrayFeatureVector<T>>> features) {
		final SparseIntFV fv = new SparseIntFV(this.assigner.size());

		for (final LocalFeature<?, ? extends ArrayFeatureVector<T>> f : features) {
			final int idx = assigner.assign(f.getFeatureVector().values);

			fv.values.increment(idx, 1);
		}

		return fv;
	}
}
