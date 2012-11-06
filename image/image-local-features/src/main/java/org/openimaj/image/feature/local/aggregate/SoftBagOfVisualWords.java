package org.openimaj.image.feature.local.aggregate;

import java.util.List;

import org.openimaj.feature.ArrayFeatureVector;
import org.openimaj.feature.SparseDoubleFV;
import org.openimaj.feature.local.LocalFeature;
import org.openimaj.ml.clustering.assignment.SoftAssigner;
import org.openimaj.util.pair.IndependentPair;

/**
 * Implementation of an object capable of extracting the soft-assigned Bag of
 * Visual Words (BoVW) representation of an image given a list of local features
 * and an {@link SoftAssigner} with an associated codebook. Soft-assignment
 * assigns a single feature to multiple visual words, usually with some
 * weighting for each word.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <DATATYPE>
 *            Primitive array type of the {@link ArrayFeatureVector}s used by
 *            the {@link LocalFeature}s that will be processed.
 * @param <DISTANCE>
 *            Primitive array datatype for recording distances between points
 *            and cluster centroids
 */
public class SoftBagOfVisualWords<DATATYPE, DISTANCE> implements VectorAggregator<ArrayFeatureVector<DATATYPE>> {
	private SoftAssigner<DATATYPE, DISTANCE> assigner;

	/**
	 * Construct with the given assigner.
	 * 
	 * @param assigner
	 *            the assigner
	 */
	public SoftBagOfVisualWords(SoftAssigner<DATATYPE, DISTANCE> assigner) {
		this.assigner = assigner;
	}

	@Override
	public SparseDoubleFV aggregate(List<? extends LocalFeature<?, ? extends ArrayFeatureVector<DATATYPE>>> features) {
		final SparseDoubleFV fv = new SparseDoubleFV();

		for (final LocalFeature<?, ? extends ArrayFeatureVector<DATATYPE>> f : features) {
			final IndependentPair<int[], DISTANCE> a = assigner.assignWeighted(f.getFeatureVector().values);

			increment(fv, a);
		}

		return fv;
	}

	private void increment(SparseDoubleFV fv, IndependentPair<int[], DISTANCE> a) {
		final int[] assignments = a.firstObject();
		final DISTANCE distances = a.getSecondObject();

		if (distances instanceof byte[]) {
			for (int i = 0; i < assignments.length; i++) {
				fv.values.increment(assignments[i], ((byte[]) distances)[i]);
			}
		} else if (distances instanceof short[]) {
			for (int i = 0; i < assignments.length; i++) {
				fv.values.increment(assignments[i], ((short[]) distances)[i]);
			}
		} else if (distances instanceof int[]) {
			for (int i = 0; i < assignments.length; i++) {
				fv.values.increment(assignments[i], ((int[]) distances)[i]);
			}
		} else if (distances instanceof long[]) {
			for (int i = 0; i < assignments.length; i++) {
				fv.values.increment(assignments[i], ((long[]) distances)[i]);
			}
		} else if (distances instanceof float[]) {
			for (int i = 0; i < assignments.length; i++) {
				fv.values.increment(assignments[i], ((float[]) distances)[i]);
			}
		} else if (distances instanceof double[]) {
			for (int i = 0; i < assignments.length; i++) {
				fv.values.increment(assignments[i], ((double[]) distances)[i]);
			}
		} else {
			throw new UnsupportedOperationException("Unsupported type");
		}
	}
}
