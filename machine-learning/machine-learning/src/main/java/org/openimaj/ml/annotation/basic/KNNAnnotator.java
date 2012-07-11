package org.openimaj.ml.annotation.basic;

import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openimaj.knn.ObjectNearestNeighbours;
import org.openimaj.knn.ObjectNearestNeighboursExact;
import org.openimaj.ml.annotation.Annotated;
import org.openimaj.ml.annotation.ScoredAnnotation;
import org.openimaj.ml.annotation.FeatureExtractor;
import org.openimaj.ml.annotation.IncrementalAnnotator;
import org.openimaj.util.comparator.DistanceComparator;

/**
 * Annotator based on a multi-class k-nearest-neighbour classifier.
 * Uses a {@link ObjectNearestNeighboursExact} to perform the kNN search,
 * so is applicable to any objects that can be compared with a
 * {@link DistanceComparator}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <O> Type of object being annotated
 * @param <A> Type of annotation
 * @param <E> Type of feature extractor
 * @param <T> Type of object produced by extractor
 */
public class KNNAnnotator<
	O,
	A,
	E extends FeatureExtractor<T, O>,
	T>
extends
	IncrementalAnnotator<O, A, E> 
{
	private int k = 1;
	private List<T> features = new ArrayList<T>();
	private List<Collection<A>> annotations = new ArrayList<Collection<A>>();
	private Set<A> annotationsSet = new HashSet<A>();
	private ObjectNearestNeighbours<T> nn;
	private DistanceComparator<T> comparator;
	
	/**
	 * Construct with the given extractor and comparator.
	 * The number of neighbours is set to 1.
	 * @param extractor the extractor
	 * @param comparator the comparator
	 */
	public KNNAnnotator(E extractor, DistanceComparator<T> comparator) {
		this(extractor, comparator, 1);
	}
	
	/**
	 * Construct with the given extractor, comparator and number
	 * of neighbours.
	 * @param extractor the extractor
	 * @param comparator the comparator
	 * @param k the number of neighbours
	 */
	public KNNAnnotator(E extractor, DistanceComparator<T> comparator, int k) {
		super(extractor);
		this.k = k;
		this.comparator = comparator;
	}

	@Override
	public void train(Annotated<O, A> annotated) {
		nn = null;
		
		features.add(extractor.extractFeature(annotated.getObject()));
		
		Collection<A> anns = annotated.getAnnotations();
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
	public Set<A> getAnnotations() {
		return annotationsSet;
	}

	@Override
	public List<ScoredAnnotation<A>> annotate(O object) {
		if (nn == null)
			nn = new ObjectNearestNeighboursExact<T>(features, comparator);
		
		TObjectIntHashMap<A> selected = new TObjectIntHashMap<A>();
		
		List<T> queryfv = new ArrayList<T>(1);
		queryfv.add(extractor.extractFeature(object));
		
		int [][] indices = new int[1][k];
		float[][] distances = new float[1][k];
		
		nn.searchKNN(queryfv, k, indices, distances);
		
		int count = 0;
		for (int i=0; i<k; i++) {
			Collection<A> anns = annotations.get(indices[0][i]);
			
			for (A ann : anns) {
				selected.increment(ann);
				count++;
			}
		}
		
		TObjectIntIterator<A> iterator = selected.iterator();
		List<ScoredAnnotation<A>> result = new ArrayList<ScoredAnnotation<A>>(selected.size());
		while (iterator.hasNext()) {
			iterator.advance();
			
			result.add(new ScoredAnnotation<A>(iterator.key(), (float)iterator.value() / (float)count));
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
	 * @param k the number of neighbours
	 */
	public void setK(int k) {
		this.k = k;
	}
}
