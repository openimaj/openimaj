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
package org.openimaj.ml.annotation.basic;

import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openimaj.feature.FeatureExtractor;
import org.openimaj.knn.ObjectNearestNeighbours;
import org.openimaj.knn.ObjectNearestNeighboursExact;
import org.openimaj.ml.annotation.Annotated;
import org.openimaj.ml.annotation.IncrementalAnnotator;
import org.openimaj.ml.annotation.ScoredAnnotation;
import org.openimaj.util.comparator.DistanceComparator;

/**
 * Annotator based on a multi-class k-nearest-neighbour classifier. Uses a
 * {@link ObjectNearestNeighboursExact} to perform the kNN search, so is
 * applicable to any objects that can be compared with a
 * {@link DistanceComparator}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <OBJECT>
 *            Type of object being annotated
 * @param <ANNOTATION>
 *            Type of annotation
 * @param <FEATURE>
 *            Type of feature produced by extractor
 */
public class KNNAnnotator<OBJECT, ANNOTATION, FEATURE>
		extends
		IncrementalAnnotator<OBJECT, ANNOTATION>
{
	protected int k = 1;
	protected final List<FEATURE> features = new ArrayList<FEATURE>();
	protected final List<Collection<ANNOTATION>> annotations = new ArrayList<Collection<ANNOTATION>>();
	protected final Set<ANNOTATION> annotationsSet = new HashSet<ANNOTATION>();
	protected ObjectNearestNeighbours<FEATURE> nn;
	protected DistanceComparator<? super FEATURE> comparator;
	protected final float threshold;
	protected FeatureExtractor<FEATURE, OBJECT> extractor;

	/**
	 * Construct with the given extractor, comparator and threshold. The number
	 * of neighbours is set to 1.
	 * <p>
	 * If the comparator defines a distance, then only scores below the distance
	 * will be accepted. If the threshold defines a similarity, then only scores
	 * above the threshold will be accepted.
	 * 
	 * @param extractor
	 *            the extractor
	 * @param comparator
	 *            the comparator
	 * @param threshold
	 *            the threshold for successful matches
	 */
	public KNNAnnotator(final FeatureExtractor<FEATURE, OBJECT> extractor,
			final DistanceComparator<? super FEATURE> comparator,
			final float threshold)
	{
		this(extractor, comparator, 1, threshold);
	}

	/**
	 * Construct with the given extractor and comparator. The number of
	 * neighbours is set to 1. The threshold test is disabled.
	 * 
	 * @param extractor
	 *            the extractor
	 * @param comparator
	 *            the comparator
	 */
	public KNNAnnotator(final FeatureExtractor<FEATURE, OBJECT> extractor,
			final DistanceComparator<? super FEATURE> comparator)
	{
		this(extractor, comparator, 1, Float.MAX_VALUE);
	}

	/**
	 * Construct with the given extractor, comparator and number of neighbours.
	 * The distance threshold is disabled.
	 * 
	 * @param extractor
	 *            the extractor
	 * @param comparator
	 *            the comparator
	 * @param k
	 *            the number of neighbours
	 */
	public KNNAnnotator(final FeatureExtractor<FEATURE, OBJECT> extractor,
			final DistanceComparator<? super FEATURE> comparator, final int k)
	{
		this(extractor, comparator, k, Float.MAX_VALUE);
	}

	/**
	 * Construct with the given extractor, comparator, number of neighbours and
	 * threshold.
	 * <p>
	 * If the comparator defines a distance, then only scores below the distance
	 * will be accepted. If the threshold defines a similarity, then only scores
	 * above the threshold will be accepted.
	 * 
	 * @param extractor
	 *            the extractor
	 * @param comparator
	 *            the comparator
	 * @param k
	 *            the number of neighbours
	 * @param threshold
	 *            the threshold on distance for successful matches
	 */
	public KNNAnnotator(final FeatureExtractor<FEATURE, OBJECT> extractor,
			final DistanceComparator<? super FEATURE> comparator, final int k,
			final float threshold)
	{
		this.extractor = extractor;
		this.comparator = comparator;
		this.k = k;
		this.threshold = comparator.isDistance() ? threshold : -threshold;
	}

	/**
	 * Create a new {@link KNNAnnotator} with the given extractor, comparator
	 * and threshold. The number of neighbours is set to 1.
	 * <p>
	 * If the comparator defines a distance, then only scores below the distance
	 * will be accepted. If the threshold defines a similarity, then only scores
	 * above the threshold will be accepted.
	 * 
	 * @param <OBJECT>
	 *            Type of object being annotated
	 * @param <ANNOTATION>
	 *            Type of annotation
	 * @param <EXTRACTOR>
	 *            Type of feature extractor
	 * @param <FEATURE>
	 *            Type of feature produced by extractor
	 * 
	 * @param extractor
	 *            the extractor
	 * @param comparator
	 *            the comparator
	 * @param threshold
	 *            the threshold for successful matches
	 * @return new {@link KNNAnnotator}
	 */
	public static <OBJECT, ANNOTATION, EXTRACTOR extends FeatureExtractor<FEATURE, OBJECT>, FEATURE>
			KNNAnnotator<OBJECT, ANNOTATION, FEATURE> create(final EXTRACTOR extractor,
					final DistanceComparator<FEATURE> comparator, final float threshold)
	{
		return new KNNAnnotator<OBJECT, ANNOTATION, FEATURE>(
				extractor, comparator, threshold);
	}

	/**
	 * Create a new {@link KNNAnnotator} with the given extractor and
	 * comparator. The number of neighbours is set to 1. The threshold test is
	 * disabled.
	 * 
	 * @param <OBJECT>
	 *            Type of object being annotated
	 * @param <ANNOTATION>
	 *            Type of annotation
	 * @param <EXTRACTOR>
	 *            Type of feature extractor
	 * @param <FEATURE>
	 *            Type of feature produced by extractor
	 * 
	 * @param extractor
	 *            the extractor
	 * @param comparator
	 *            the comparator
	 * @return new {@link KNNAnnotator}
	 */
	public static <OBJECT, ANNOTATION, EXTRACTOR extends FeatureExtractor<FEATURE, OBJECT>, FEATURE>
			KNNAnnotator<OBJECT, ANNOTATION, FEATURE> create(final EXTRACTOR extractor,
					final DistanceComparator<FEATURE> comparator)
	{
		return new KNNAnnotator<OBJECT, ANNOTATION, FEATURE>(
				extractor, comparator);
	}

	/**
	 * Create a new {@link KNNAnnotator} with the given extractor, comparator
	 * and number of neighbours. The distance threshold is disabled.
	 * 
	 * @param <OBJECT>
	 *            Type of object being annotated
	 * @param <ANNOTATION>
	 *            Type of annotation
	 * @param <EXTRACTOR>
	 *            Type of feature extractor
	 * @param <FEATURE>
	 *            Type of feature produced by extractor
	 * 
	 * @param extractor
	 *            the extractor
	 * @param comparator
	 *            the comparator
	 * @param k
	 *            the number of neighbours
	 * @return new {@link KNNAnnotator}
	 */
	public static <OBJECT, ANNOTATION, EXTRACTOR extends FeatureExtractor<FEATURE, OBJECT>, FEATURE>
			KNNAnnotator<OBJECT, ANNOTATION, FEATURE> create(final EXTRACTOR extractor,
					final DistanceComparator<FEATURE> comparator, final int k)
	{
		return new KNNAnnotator<OBJECT, ANNOTATION, FEATURE>(
				extractor, comparator, k);
	}

	/**
	 * Create a new {@link KNNAnnotator} with the given extractor, comparator,
	 * number of neighbours and threshold.
	 * <p>
	 * If the comparator defines a distance, then only scores below the distance
	 * will be accepted. If the threshold defines a similarity, then only scores
	 * above the threshold will be accepted.
	 * 
	 * @param <OBJECT>
	 *            Type of object being annotated
	 * @param <ANNOTATION>
	 *            Type of annotation
	 * @param <EXTRACTOR>
	 *            Type of feature extractor
	 * @param <FEATURE>
	 *            Type of feature produced by extractor
	 * 
	 * @param extractor
	 *            the extractor
	 * @param comparator
	 *            the comparator
	 * @param k
	 *            the number of neighbours
	 * @param threshold
	 *            the threshold on distance for successful matches
	 * @return new {@link KNNAnnotator}
	 */
	public static <OBJECT, ANNOTATION, EXTRACTOR extends FeatureExtractor<FEATURE, OBJECT>, FEATURE>
			KNNAnnotator<OBJECT, ANNOTATION, FEATURE> create(final EXTRACTOR extractor,
					final DistanceComparator<FEATURE> comparator, final int k, final float threshold)
	{
		return new KNNAnnotator<OBJECT, ANNOTATION, FEATURE>(
				extractor, comparator, k, threshold);
	}

	@Override
	public void train(final Annotated<OBJECT, ANNOTATION> annotated) {
		this.nn = null;

		this.features.add(this.extractor.extractFeature(annotated.getObject()));

		final Collection<ANNOTATION> anns = annotated.getAnnotations();
		this.annotations.add(anns);
		this.annotationsSet.addAll(anns);
	}

	@Override
	public void reset() {
		this.nn = null;
		this.features.clear();
		this.annotations.clear();
		this.annotationsSet.clear();
	}

	@Override
	public Set<ANNOTATION> getAnnotations() {
		return this.annotationsSet;
	}

	@Override
	public List<ScoredAnnotation<ANNOTATION>> annotate(final OBJECT object) {
		if (this.nn == null)
			this.nn = new ObjectNearestNeighboursExact<FEATURE>(this.features, this.comparator);

		final TObjectIntHashMap<ANNOTATION> selected = new TObjectIntHashMap<ANNOTATION>();

		final List<FEATURE> queryfv = new ArrayList<FEATURE>(1);
		queryfv.add(this.extractor.extractFeature(object));

		final int[][] indices = new int[1][this.k];
		final float[][] distances = new float[1][this.k];

		this.nn.searchKNN(queryfv, this.k, indices, distances);

		int count = 0;
		for (int i = 0; i < this.k; i++) {
			// Distance check
			if (distances[0][i] > this.threshold) {
				continue;
			}

			final Collection<ANNOTATION> anns = this.annotations.get(indices[0][i]);

			for (final ANNOTATION ann : anns) {
				selected.adjustOrPutValue(ann, 1, 1);
				count++;
			}
		}

		final TObjectIntIterator<ANNOTATION> iterator = selected.iterator();
		final List<ScoredAnnotation<ANNOTATION>> result = new ArrayList<ScoredAnnotation<ANNOTATION>>(selected.size());
		while (iterator.hasNext()) {
			iterator.advance();

			result.add(new ScoredAnnotation<ANNOTATION>(iterator.key(), (float) iterator.value() / (float) count));
		}

		return result;
	}

	/**
	 * @return the number of neighbours to search for
	 */
	public int getK() {
		return this.k;
	}

	/**
	 * Set the number of neighbours
	 * 
	 * @param k
	 *            the number of neighbours
	 */
	public void setK(final int k) {
		this.k = k;
	}
}
