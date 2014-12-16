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
public class SoftBagOfVisualWords<DATATYPE, DISTANCE>
		implements
		VectorAggregator<ArrayFeatureVector<DATATYPE>, SparseDoubleFV>
{
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
		final SparseDoubleFV fv = new SparseDoubleFV(assigner.numDimensions());

		for (final LocalFeature<?, ? extends ArrayFeatureVector<DATATYPE>> f : features) {
			final IndependentPair<int[], DISTANCE> a = assigner.assignWeighted(f.getFeatureVector().values);

			increment(fv, a);
		}

		return fv;
	}

	@Override
	public SparseDoubleFV aggregateVectors(List<? extends ArrayFeatureVector<DATATYPE>> features) {
		final SparseDoubleFV fv = new SparseDoubleFV(assigner.numDimensions());

		for (final ArrayFeatureVector<DATATYPE> f : features) {
			final IndependentPair<int[], DISTANCE> a = assigner.assignWeighted(f.values);

			increment(fv, a);
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
	public SparseDoubleFV aggregateVectorsRaw(List<DATATYPE> features) {
		final SparseDoubleFV fv = new SparseDoubleFV(assigner.numDimensions());

		for (final DATATYPE f : features) {
			final IndependentPair<int[], DISTANCE> a = assigner.assignWeighted(f);

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
