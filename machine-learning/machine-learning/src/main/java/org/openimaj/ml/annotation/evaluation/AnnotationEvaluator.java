package org.openimaj.ml.annotation.evaluation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.openimaj.experiment.dataset.Dataset;
import org.openimaj.experiment.dataset.Identifiable;
import org.openimaj.experiment.evaluation.AnalysisResult;
import org.openimaj.experiment.evaluation.classification.BasicClassificationResult;
import org.openimaj.experiment.evaluation.classification.ClassificationAnalyser;
import org.openimaj.experiment.evaluation.classification.ClassificationEvaluator;
import org.openimaj.experiment.evaluation.classification.ClassificationResult;
import org.openimaj.experiment.evaluation.classification.Classifier;
import org.openimaj.experiment.evaluation.retrieval.RetrievalAnalyser;
import org.openimaj.experiment.evaluation.retrieval.RetrievalEngine;
import org.openimaj.experiment.evaluation.retrieval.RetrievalEvaluator;
import org.openimaj.ml.annotation.Annotated;
import org.openimaj.ml.annotation.Annotator;
import org.openimaj.ml.annotation.AutoAnnotation;
import org.openimaj.util.pair.ObjectDoublePair;

/**
 * A class to help evaluate the performance of an {@link Annotator}
 * using standardised classification and/or retrieval evaluation
 * methodologies. 
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <OBJECT> Type of object being annotated
 * @param <ANNOTATION> Type of annotation.
 */
public class AnnotationEvaluator<
	OBJECT extends Identifiable,
	ANNOTATION>
{
	Annotator<OBJECT, ANNOTATION, ?> annotator;
	Dataset<? extends Annotated<OBJECT, ANNOTATION>> testData;
	AnnotationEvaluationEngine<OBJECT, ANNOTATION> engine;

	/**
	 * Construct a new {@link AnnotationEvaluator} with the given
	 * annotator and test data (with ground-truth annotations).
	 * 
	 * @param annotator the annotator
	 * @param testData the test data with ground-truth annotations.
	 */
	public AnnotationEvaluator(Annotator<OBJECT, ANNOTATION, ?> annotator, Dataset<? extends Annotated<OBJECT, ANNOTATION>> testData) {
		this.annotator = annotator;
		this.testData = testData;
		engine = new AnnotationEvaluationEngine<OBJECT, ANNOTATION>(annotator, testData);
	}
	
	/**
	 * Make a new {@link ClassificationEvaluator}, backed by the
	 * annotations computed by this {@link AnnotationEvaluator},
	 * with the given {@link ClassificationAnalyser}.
	 * 
	 * @param <RESULT> The type of {@link AnalysisResult} produced by the evaluator
	 * @param analyser the ClassificationAnalyser
	 * @return the evaluator
	 */
	public <RESULT extends AnalysisResult> ClassificationEvaluator<RESULT, ANNOTATION, OBJECT> newClassificationEvaluator(ClassificationAnalyser<RESULT, ANNOTATION, OBJECT> analyser) {
		return new ClassificationEvaluator<RESULT, ANNOTATION, OBJECT>(engine, getObjects(), getActual(), analyser);
	}

	/**
	 * Make a new {@link RetrievalEvaluator}, backed by the
	 * annotations computed by this {@link AnnotationEvaluator},
	 * with the given {@link RetrievalAnalyser}.
	 * 
	 * @param <RESULT> The type of {@link AnalysisResult} produced by the evaluator
	 * @param analyser the RetrievalAnalyser
	 * @return the evaluator
	 */
	public <RESULT extends AnalysisResult> RetrievalEvaluator<RESULT, OBJECT, ANNOTATION> newRetrievalEvaluator(RetrievalAnalyser<RESULT, ANNOTATION, OBJECT> analyser) {
		Set<ANNOTATION> queries = this.getQueries();
		Map<ANNOTATION, Set<OBJECT>> relevant = this.getRelevant(queries);

		return new RetrievalEvaluator<RESULT, OBJECT, ANNOTATION>(engine, relevant, analyser);
	}

	/**
	 * @return the objects for constructing a {@link ClassificationEvaluator}
	 */
	private Collection<OBJECT> getObjects() {
		List<OBJECT> objects = new ArrayList<OBJECT>();

		for (Annotated<OBJECT, ANNOTATION> ao : testData) {
			objects.add(ao.getObject());
		}

		return objects;
	}

	/**
	 * @return the actual classes for constructing a {@link ClassificationEvaluator}
	 */
	private Map<OBJECT, Set<ANNOTATION>> getActual() {
		Map<OBJECT, Set<ANNOTATION>> actual = new HashMap<OBJECT, Set<ANNOTATION>>();

		for (Annotated<OBJECT, ANNOTATION> ao : testData) {
			actual.put(ao.getObject(), new HashSet<ANNOTATION>(ao.getAnnotations()));
		}

		return actual;
	}

	/**
	 * @return the queries for constructing a {@link RetrievalEvaluator}
	 */
	private Set<ANNOTATION> getQueries() {
		Set<ANNOTATION> testAnnotations = new HashSet<ANNOTATION>();

		for (Annotated<OBJECT, ANNOTATION> item : testData) {
			testAnnotations.addAll(item.getAnnotations());
		}

		testAnnotations.retainAll(annotator.getAnnotations());

		return testAnnotations;
	}

	/**
	 * @return the relevant docs for constructing a {@link RetrievalEvaluator}
	 */
	private Map<ANNOTATION, Set<OBJECT>> getRelevant(Collection<ANNOTATION> queries) {
		Map<ANNOTATION, Set<OBJECT>> relevant = new HashMap<ANNOTATION, Set<OBJECT>>();

		for (ANNOTATION query : queries) {
			HashSet<OBJECT> rset = new HashSet<OBJECT>();
			relevant.put(query, rset);

			for (Annotated<OBJECT, ANNOTATION> item : testData) {
				if (item.getAnnotations().contains(query)) {
					rset.add(item.getObject());
				}
			}
		}

		return relevant;
	}

	static class AnnotationEvaluationEngine<
		OBJECT extends Identifiable,
		ANNOTATION> 
	implements 
		RetrievalEngine<OBJECT, ANNOTATION>,
		Classifier<ANNOTATION, OBJECT>
	{
		Map<OBJECT, List<AutoAnnotation<ANNOTATION>>> results = new HashMap<OBJECT, List<AutoAnnotation<ANNOTATION>>>();

		public AnnotationEvaluationEngine(Annotator<OBJECT, ANNOTATION, ?> annotator, Dataset<? extends Annotated<OBJECT, ANNOTATION>> testData) {
			for (Annotated<OBJECT, ANNOTATION> item : testData) {
				OBJECT obj = item.getObject();
				results.put(obj, annotator.annotate(obj));
			}
		}

		@Override
		public List<OBJECT> search(ANNOTATION query) {
			List<ObjectDoublePair<OBJECT>> sr = new ArrayList<ObjectDoublePair<OBJECT>>();

			for (Entry<OBJECT, List<AutoAnnotation<ANNOTATION>>> e : results.entrySet()) {
				for (AutoAnnotation<ANNOTATION> a : e.getValue()) {
					if (a.annotation.equals(query)) {
						sr.add(ObjectDoublePair.pair(e.getKey(), a.confidence));
						break;
					}
				}
			}

			Collections.sort(sr, new Comparator<ObjectDoublePair<OBJECT>>() {
				@Override
				public int compare(ObjectDoublePair<OBJECT> o1, ObjectDoublePair<OBJECT> o2) {
					if (o1.second == o2.second) return 0;
					if (o1.second < o2.second) return 1;
					return -1;
				}
			});

			return ObjectDoublePair.getFirst(sr);
		}

		@Override
		public ClassificationResult<ANNOTATION> classify(OBJECT object) {
			BasicClassificationResult<ANNOTATION> res = new BasicClassificationResult<ANNOTATION>();

			for (AutoAnnotation<ANNOTATION> anno : results.get(object)) {
				res.put(anno.annotation, anno.confidence);
			}

			return res;
		}
	}
}

