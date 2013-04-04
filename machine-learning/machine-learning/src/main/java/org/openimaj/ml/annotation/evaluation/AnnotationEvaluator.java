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

import org.openimaj.data.dataset.Dataset;
import org.openimaj.data.identity.Identifiable;
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
import org.openimaj.ml.annotation.ScoredAnnotation;
import org.openimaj.util.pair.ObjectDoublePair;

/**
 * A class to help evaluate the performance of an {@link Annotator} using
 * standardised classification and/or retrieval evaluation methodologies.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <OBJECT>
 *            Type of object being annotated
 * @param <ANNOTATION>
 *            Type of annotation.
 */
public class AnnotationEvaluator<OBJECT extends Identifiable, ANNOTATION>
{
	Annotator<OBJECT, ANNOTATION> annotator;
	Dataset<? extends Annotated<OBJECT, ANNOTATION>> testData;
	AnnotationEvaluationEngine<OBJECT, ANNOTATION> engine;

	/**
	 * Construct a new {@link AnnotationEvaluator} with the given annotator and
	 * test data (with ground-truth annotations).
	 * 
	 * @param annotator
	 *            the annotator
	 * @param testData
	 *            the test data with ground-truth annotations.
	 */
	public AnnotationEvaluator(Annotator<OBJECT, ANNOTATION> annotator,
			Dataset<? extends Annotated<OBJECT, ANNOTATION>> testData)
	{
		this.annotator = annotator;
		this.testData = testData;
		engine = new AnnotationEvaluationEngine<OBJECT, ANNOTATION>(annotator, testData);
	}

	/**
	 * Make a new {@link ClassificationEvaluator}, backed by the annotations
	 * computed by this {@link AnnotationEvaluator}, with the given
	 * {@link ClassificationAnalyser}.
	 * 
	 * @param <RESULT>
	 *            The type of {@link AnalysisResult} produced by the evaluator
	 * @param analyser
	 *            the ClassificationAnalyser
	 * @return the evaluator
	 */
	public <RESULT extends AnalysisResult>
			ClassificationEvaluator<RESULT, ANNOTATION, OBJECT>
			newClassificationEvaluator(ClassificationAnalyser<RESULT, ANNOTATION, OBJECT> analyser)
	{
		return new ClassificationEvaluator<RESULT, ANNOTATION, OBJECT>(engine, getObjects(), getActual(), analyser);
	}

	/**
	 * Make a new {@link RetrievalEvaluator}, backed by the annotations computed
	 * by this {@link AnnotationEvaluator}, with the given
	 * {@link RetrievalAnalyser}.
	 * 
	 * @param <RESULT>
	 *            The type of {@link AnalysisResult} produced by the evaluator
	 * @param analyser
	 *            the RetrievalAnalyser
	 * @return the evaluator
	 */
	public <RESULT extends AnalysisResult> RetrievalEvaluator<RESULT, OBJECT, ANNOTATION> newRetrievalEvaluator(
			RetrievalAnalyser<RESULT, ANNOTATION, OBJECT> analyser)
	{
		final Set<ANNOTATION> queries = this.getQueries();
		final Map<ANNOTATION, Set<OBJECT>> relevant = this.getRelevant(queries);

		return new RetrievalEvaluator<RESULT, OBJECT, ANNOTATION>(engine, relevant, analyser);
	}

	/**
	 * @return the objects for constructing a {@link ClassificationEvaluator}
	 */
	private Collection<OBJECT> getObjects() {
		final List<OBJECT> objects = new ArrayList<OBJECT>();

		for (final Annotated<OBJECT, ANNOTATION> ao : testData) {
			objects.add(ao.getObject());
		}

		return objects;
	}

	/**
	 * @return the actual classes for constructing a
	 *         {@link ClassificationEvaluator}
	 */
	private Map<OBJECT, Set<ANNOTATION>> getActual() {
		final Map<OBJECT, Set<ANNOTATION>> actual = new HashMap<OBJECT, Set<ANNOTATION>>();

		for (final Annotated<OBJECT, ANNOTATION> ao : testData) {
			actual.put(ao.getObject(), new HashSet<ANNOTATION>(ao.getAnnotations()));
		}

		return actual;
	}

	/**
	 * @return the queries for constructing a {@link RetrievalEvaluator}
	 */
	private Set<ANNOTATION> getQueries() {
		final Set<ANNOTATION> testAnnotations = new HashSet<ANNOTATION>();

		for (final Annotated<OBJECT, ANNOTATION> item : testData) {
			testAnnotations.addAll(item.getAnnotations());
		}

		testAnnotations.retainAll(annotator.getAnnotations());

		return testAnnotations;
	}

	/**
	 * @return the relevant docs for constructing a {@link RetrievalEvaluator}
	 */
	private Map<ANNOTATION, Set<OBJECT>> getRelevant(Collection<ANNOTATION> queries) {
		final Map<ANNOTATION, Set<OBJECT>> relevant = new HashMap<ANNOTATION, Set<OBJECT>>();

		for (final ANNOTATION query : queries) {
			final HashSet<OBJECT> rset = new HashSet<OBJECT>();
			relevant.put(query, rset);

			for (final Annotated<OBJECT, ANNOTATION> item : testData) {
				if (item.getAnnotations().contains(query)) {
					rset.add(item.getObject());
				}
			}
		}

		return relevant;
	}

	static class AnnotationEvaluationEngine<OBJECT extends Identifiable, ANNOTATION>
			implements
			RetrievalEngine<OBJECT, ANNOTATION>,
			Classifier<ANNOTATION, OBJECT>
	{
		Map<OBJECT, List<ScoredAnnotation<ANNOTATION>>> results = new HashMap<OBJECT, List<ScoredAnnotation<ANNOTATION>>>();

		public AnnotationEvaluationEngine(Annotator<OBJECT, ANNOTATION> annotator,
				Dataset<? extends Annotated<OBJECT, ANNOTATION>> testData)
		{
			for (final Annotated<OBJECT, ANNOTATION> item : testData) {
				final OBJECT obj = item.getObject();
				results.put(obj, annotator.annotate(obj));
			}
		}

		@Override
		public List<OBJECT> search(ANNOTATION query) {
			final List<ObjectDoublePair<OBJECT>> sr = new ArrayList<ObjectDoublePair<OBJECT>>();

			for (final Entry<OBJECT, List<ScoredAnnotation<ANNOTATION>>> e : results.entrySet()) {
				for (final ScoredAnnotation<ANNOTATION> a : e.getValue()) {
					if (a.annotation.equals(query)) {
						sr.add(ObjectDoublePair.pair(e.getKey(), a.confidence));
						break;
					}
				}
			}

			Collections.sort(sr, new Comparator<ObjectDoublePair<OBJECT>>() {
				@Override
				public int compare(ObjectDoublePair<OBJECT> o1, ObjectDoublePair<OBJECT> o2) {
					if (o1.second == o2.second)
						return 0;
					if (o1.second < o2.second)
						return 1;
					return -1;
				}
			});

			return ObjectDoublePair.getFirst(sr);
		}

		@Override
		public ClassificationResult<ANNOTATION> classify(OBJECT object) {
			final BasicClassificationResult<ANNOTATION> res = new BasicClassificationResult<ANNOTATION>();

			for (final ScoredAnnotation<ANNOTATION> anno : results.get(object)) {
				res.put(anno.annotation, anno.confidence);
			}

			return res;
		}
	}
}
