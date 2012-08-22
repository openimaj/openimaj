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
 * @param <EXTRACTOR>
 *            Type of feature extractor
 * @param <FEATURE>
 *            Type of feature produced by extractor
 */
public class KNNAnnotator<OBJECT, ANNOTATION, EXTRACTOR extends FeatureExtractor<FEATURE, OBJECT>, FEATURE>
		extends
		IncrementalAnnotator<OBJECT, ANNOTATION, EXTRACTOR>
{
	private int k = 1;
	private List<FEATURE> features = new ArrayList<FEATURE>();
	private List<Collection<ANNOTATION>> annotations = new ArrayList<Collection<ANNOTATION>>();
	private Set<ANNOTATION> annotationsSet = new HashSet<ANNOTATION>();
	private ObjectNearestNeighbours<FEATURE> nn;
	private DistanceComparator<FEATURE> comparator;
	private float threshold = 0;

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
	public KNNAnnotator(EXTRACTOR extractor, DistanceComparator<FEATURE> comparator, float threshold) {
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
	public KNNAnnotator(EXTRACTOR extractor, DistanceComparator<FEATURE> comparator) {
		this(extractor, comparator, 1, comparator.isDistance() ? Float.MAX_VALUE : -Float.MAX_VALUE);
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
	public KNNAnnotator(EXTRACTOR extractor, DistanceComparator<FEATURE> comparator, int k) {
		this(extractor, comparator, k, comparator.isDistance() ? Float.MAX_VALUE : -Float.MAX_VALUE);
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
	 *            the threshold for successful matches
	 */
	public KNNAnnotator(EXTRACTOR extractor, DistanceComparator<FEATURE> comparator, int k, float threshold) {
		super(extractor);
		this.comparator = comparator;
		this.k = k;
		this.threshold = threshold;
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
			KNNAnnotator<OBJECT, ANNOTATION, EXTRACTOR, FEATURE> create(EXTRACTOR extractor,
					DistanceComparator<FEATURE> comparator, float threshold)
	{
		return new KNNAnnotator<OBJECT, ANNOTATION, EXTRACTOR, FEATURE>(
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
			KNNAnnotator<OBJECT, ANNOTATION, EXTRACTOR, FEATURE> create(EXTRACTOR extractor,
					DistanceComparator<FEATURE> comparator)
	{
		return new KNNAnnotator<OBJECT, ANNOTATION, EXTRACTOR, FEATURE>(
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
			KNNAnnotator<OBJECT, ANNOTATION, EXTRACTOR, FEATURE> create(EXTRACTOR extractor,
					DistanceComparator<FEATURE> comparator, int k)
	{
		return new KNNAnnotator<OBJECT, ANNOTATION, EXTRACTOR, FEATURE>(
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
	 *            the threshold for successful matches
	 * @return new {@link KNNAnnotator}
	 */
	public static <OBJECT, ANNOTATION, EXTRACTOR extends FeatureExtractor<FEATURE, OBJECT>, FEATURE>
			KNNAnnotator<OBJECT, ANNOTATION, EXTRACTOR, FEATURE> create(EXTRACTOR extractor,
					DistanceComparator<FEATURE> comparator, int k, float threshold)
	{
		return new KNNAnnotator<OBJECT, ANNOTATION, EXTRACTOR, FEATURE>(
				extractor, comparator, k, threshold);
	}

	@Override
	public void train(Annotated<OBJECT, ANNOTATION> annotated) {
		nn = null;

		features.add(extractor.extractFeature(annotated.getObject()));

		final Collection<ANNOTATION> anns = annotated.getAnnotations();
		annotations.add(anns);
		annotationsSet.addAll(anns);
	}

	@Override
	public void reset() {
		nn = null;
		features.clear();
		annotations.clear();
		annotationsSet.clear();
	}

	@Override
	public Set<ANNOTATION> getAnnotations() {
		return annotationsSet;
	}

	@Override
	public List<ScoredAnnotation<ANNOTATION>> annotate(OBJECT object) {
		if (nn == null)
			nn = new ObjectNearestNeighboursExact<FEATURE>(features, comparator);

		final TObjectIntHashMap<ANNOTATION> selected = new TObjectIntHashMap<ANNOTATION>();

		final List<FEATURE> queryfv = new ArrayList<FEATURE>(1);
		queryfv.add(extractor.extractFeature(object));

		final int[][] indices = new int[1][k];
		final float[][] distances = new float[1][k];

		nn.searchKNN(queryfv, k, indices, distances);

		int count = 0;
		for (int i = 0; i < k; i++) {
			// Distance check
			if (comparator.isDistance()) {
				if (distances[0][i] > threshold) {
					continue;
				}
			} else {
				if (distances[0][i] < threshold) {
					continue;
				}
			}

			final Collection<ANNOTATION> anns = annotations.get(indices[0][i]);

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
		return k;
	}

	/**
	 * Set the number of neighbours
	 * 
	 * @param k
	 *            the number of neighbours
	 */
	public void setK(int k) {
		this.k = k;
	}
}
