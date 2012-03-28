package org.openimaj.ml.annotation.evaluation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openimaj.experiment.dataset.Dataset;
import org.openimaj.experiment.dataset.Identifiable;
import org.openimaj.experiment.evaluation.retrieval.RetrievalAnalyser;
import org.openimaj.experiment.evaluation.retrieval.RetrievalEngine;
import org.openimaj.experiment.evaluation.retrieval.RetrievalEvaluator;
import org.openimaj.ml.annotation.Annotated;
import org.openimaj.ml.annotation.Annotator;
import org.openimaj.ml.annotation.AutoAnnotation;
import org.openimaj.util.pair.ObjectDoublePair;

//given a trained annotator and a set of test documents WITH ground truth
//annotations, evaluate the effectiveness of auto-annotation
//as a retrieval experiment.
public class AnnotatorRetrievalEvaluator<O, A, R, T extends Annotated<O, A> & Identifiable> extends RetrievalEvaluator<R, T, A>{
	private class Engine implements RetrievalEngine<T, A> {
		Map<T, List<AutoAnnotation<A>>> results = new HashMap<T, List<AutoAnnotation<A>>>();
		
		public Engine(Annotator<O, A, ?> annotator, Dataset<T> testData) {
			for (T item : testData) {
				results.put(item, annotator.annotate(item.getObject()));
			}
		}
		
		@Override
		public List<T> search(A query) {
			List<ObjectDoublePair<T>> sr = new ArrayList<ObjectDoublePair<T>>();
			
			for (Entry<T, List<AutoAnnotation<A>>> e : results.entrySet()) {
				for (AutoAnnotation<A> a : e.getValue()) {
					if (a.annotation.equals(query)) {
						sr.add(ObjectDoublePair.pair(e.getKey(), a.confidence));
						break;
					}
				}
			}
			
			Collections.sort(sr, new Comparator<ObjectDoublePair<T>>() {
				@Override
				public int compare(ObjectDoublePair<T> o1, ObjectDoublePair<T> o2) {
					if (o1.second == o2.second) return 0;
					if (o1.second < o2.second) return 1;
					return -1;
				}
			});
			
			return ObjectDoublePair.getFirst(sr);
		}
	}
	
	public AnnotatorRetrievalEvaluator(Annotator<O, A, ?> annotator, Dataset<T> testData, RetrievalAnalyser<R, A, T> analyser) {
		super(null, null, null, analyser);
		
		computeQueries(annotator.getAnnotations(), testData);
		computeRelevant(queries, testData);
		this.engine = new Engine(annotator, testData);
	}

	private void computeQueries(Set<A> annotations, Dataset<T> testData) {
		Set<A> testAnnotations = new HashSet<A>();
		
		for (T item : testData) {
			testAnnotations.addAll(item.getAnnotations());
		}
		
		testAnnotations.retainAll(annotations);
		
		this.queries = testAnnotations;
	}

	private void computeRelevant(Collection<A> queries, Dataset<T> testData) {
		relevant = new HashMap<A, Set<T>>();
		
		for (A query : queries) {
			HashSet<T> rset = new HashSet<T>();
			relevant.put(query, rset);
			
			for (T item : testData) {
				if (item.getAnnotations().contains(query)) {
					rset.add(item);
				}
			}
		}
	}
}
