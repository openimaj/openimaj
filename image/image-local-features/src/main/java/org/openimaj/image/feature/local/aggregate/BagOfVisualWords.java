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
 *            Primitive array type of the {@link ArrayFeatureVector}s used by
 *            the {@link LocalFeature}s that will be processed.
 */
public class BagOfVisualWords<T> implements VectorAggregator<ArrayFeatureVector<T>, SparseIntFV> {
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
			List<? extends LocalFeature<L, ? extends ArrayFeatureVector<T>>> features)
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
	 * @param nfeatures
	 *            the number of visual words.
	 * @return a histogram of the occurrences of the visual words
	 */
	public static <L extends Location>
			SparseIntFV
			extractFeatureFromQuantised(Collection<? extends QuantisedLocalFeature<L>> qfeatures, final int nfeatures)
	{
		final SparseIntFV fv = new SparseIntFV(nfeatures);

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

	@Override
	public SparseIntFV aggregateVectors(List<? extends ArrayFeatureVector<T>> features) {
		final SparseIntFV fv = new SparseIntFV(this.assigner.size());

		for (final ArrayFeatureVector<T> f : features) {
			final int idx = assigner.assign(f.values);

			fv.values.increment(idx, 1);
		}

		return fv;
	}

	/**
	 * Aggregate the given features into a vector.
	 * 
	 * @param features
	 *            the features to aggregate
	 * @return the aggregated vector
	 */
	public SparseIntFV aggregateVectorsRaw(List<T> features) {
		final SparseIntFV fv = new SparseIntFV(this.assigner.size());

		for (final T f : features) {
			final int idx = assigner.assign(f);

			fv.values.increment(idx, 1);
		}

		return fv;
	}
}
