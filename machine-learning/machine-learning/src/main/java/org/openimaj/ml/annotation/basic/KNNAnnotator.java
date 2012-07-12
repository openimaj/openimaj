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
import org.openimaj.ml.annotation.IncrementalAnnotator;
import org.openimaj.ml.feature.FeatureExtractor;
import org.openimaj.util.comparator.DistanceComparator;

/**
 * Annotator based on a multi-class k-nearest-neighbour classifier.
 * Uses a {@link ObjectNearestNeighboursExact} to perform the kNN search,
 * so is applicable to any objects that can be compared with a
 * {@link DistanceComparator}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <OBJECT> Type of object being annotated
 * @param <ANNOTATION> Type of annotation
 * @param <EXTRACTOR> Type of feature extractor
 * @param <FEATURE> Type of feature produced by extractor
 */
public class KNNAnnotator<
	OBJECT,
	ANNOTATION,
	EXTRACTOR extends FeatureExtractor<FEATURE, OBJECT>,
	FEATURE>
extends
	IncrementalAnnotator<OBJECT, ANNOTATION, EXTRACTOR> 
{
	private int k = 1;
	private List<FEATURE> features = new ArrayList<FEATURE>();
	private List<Collection<ANNOTATION>> annotations = new ArrayList<Collection<ANNOTATION>>();
	private Set<ANNOTATION> annotationsSet = new HashSet<ANNOTATION>();
	private ObjectNearestNeighbours<FEATURE> nn;
	private DistanceComparator<FEATURE> comparator;
	
	/**
	 * Construct with the given extractor and comparator.
	 * The number of neighbours is set to 1.
	 * @param extractor the extractor
	 * @param comparator the comparator
	 */
	public KNNAnnotator(EXTRACTOR extractor, DistanceComparator<FEATURE> comparator) {
		this(extractor, comparator, 1);
	}
	
	/**
	 * Construct with the given extractor, comparator and number
	 * of neighbours.
	 * @param extractor the extractor
	 * @param comparator the comparator
	 * @param k the number of neighbours
	 */
	public KNNAnnotator(EXTRACTOR extractor, DistanceComparator<FEATURE> comparator, int k) {
		super(extractor);
		this.k = k;
		this.comparator = comparator;
	}

	@Override
	public void train(Annotated<OBJECT, ANNOTATION> annotated) {
		nn = null;
		
		features.add(extractor.extractFeature(annotated.getObject()));
		
		Collection<ANNOTATION> anns = annotated.getAnnotations();
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
		
		TObjectIntHashMap<ANNOTATION> selected = new TObjectIntHashMap<ANNOTATION>();
		
		List<FEATURE> queryfv = new ArrayList<FEATURE>(1);
		queryfv.add(extractor.extractFeature(object));
		
		int [][] indices = new int[1][k];
		float[][] distances = new float[1][k];
		
		nn.searchKNN(queryfv, k, indices, distances);
		
		int count = 0;
		for (int i=0; i<k; i++) {
			Collection<ANNOTATION> anns = annotations.get(indices[0][i]);
			
			for (ANNOTATION ann : anns) {
				selected.adjustOrPutValue(ann, 1, 1);
				count++;
			}
		}
		
		TObjectIntIterator<ANNOTATION> iterator = selected.iterator();
		List<ScoredAnnotation<ANNOTATION>> result = new ArrayList<ScoredAnnotation<ANNOTATION>>(selected.size());
		while (iterator.hasNext()) {
			iterator.advance();
			
			result.add(new ScoredAnnotation<ANNOTATION>(iterator.key(), (float)iterator.value() / (float)count));
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
