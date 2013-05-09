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
package org.openimaj.experiment.evaluation.classification;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.experiment.dataset.util.DatasetAdaptors;
import org.openimaj.experiment.evaluation.AnalysisResult;
import org.openimaj.experiment.evaluation.Evaluator;

/**
 * Implementation of an {@link Evaluator} for the evaluation of classification
 * experiments.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <RESULT>
 *            Type of analysed data
 * @param <CLASS>
 *            Type of classes predicted by the classifier
 * @param <OBJECT>
 *            Type of objects classified by the classifier
 */
public class ClassificationEvaluator<RESULT extends AnalysisResult, CLASS, OBJECT>
		implements Evaluator<
		Map<OBJECT, ClassificationResult<CLASS>>, RESULT>
{
	protected Classifier<CLASS, OBJECT> classifier;
	protected ClassificationAnalyser<RESULT, CLASS, OBJECT> analyser;
	protected Map<OBJECT, Set<CLASS>> actual;
	protected Collection<OBJECT> objects;

	/**
	 * Construct a new {@link ClassificationEvaluator} with the given
	 * classifier, set of objects to classify, ground truth ("actual") data and
	 * an {@link ClassificationAnalyser}.
	 * 
	 * @param classifier
	 *            the classifier
	 * @param objects
	 *            the objects to classify
	 * @param actual
	 *            the ground truth
	 * @param analyser
	 *            the analyser
	 */
	public ClassificationEvaluator(Classifier<CLASS, OBJECT> classifier, Collection<OBJECT> objects,
			Map<OBJECT, Set<CLASS>> actual, ClassificationAnalyser<RESULT, CLASS, OBJECT> analyser)
	{
		this.classifier = classifier;
		this.objects = objects;
		this.actual = actual;
		this.analyser = analyser;
	}

	/**
	 * Construct a new {@link ClassificationEvaluator} with the given
	 * classifier, ground truth ("actual") data and an
	 * {@link ClassificationAnalyser}.
	 * <p>
	 * The objects to classify are taken from the {@link Map#keySet()} of the
	 * ground truth.
	 * 
	 * @param classifier
	 *            the classifier
	 * @param actual
	 *            the ground truth
	 * @param analyser
	 *            the analyser
	 */
	public ClassificationEvaluator(Classifier<CLASS, OBJECT> classifier, Map<OBJECT, Set<CLASS>> actual,
			ClassificationAnalyser<RESULT, CLASS, OBJECT> analyser)
	{
		this.classifier = classifier;
		this.objects = actual.keySet();
		this.actual = actual;
		this.analyser = analyser;
	}

	/**
	 * Construct a new {@link ClassificationEvaluator} with the given
	 * classifier, ground truth ("actual") data and an
	 * {@link ClassificationAnalyser}.
	 * <p>
	 * The ground-truth classes to are taken from the
	 * {@link GroupedDataset#getGroups()} of the "actual" {@link GroupedDataset}
	 * , and the objects are assembled by concatenating all of the
	 * {@link ListDataset}s within the "actual" dataset.
	 * 
	 * @param classifier
	 *            the classifier
	 * @param actual
	 *            the dataset containing instances and ground truths
	 * @param analyser
	 *            the analyser
	 */
	public ClassificationEvaluator(Classifier<CLASS, OBJECT> classifier,
			GroupedDataset<CLASS, ? extends ListDataset<OBJECT>, OBJECT> actual,
			ClassificationAnalyser<RESULT, CLASS, OBJECT> analyser)
	{
		this.classifier = classifier;
		this.objects = DatasetAdaptors.asList(actual);
		this.actual = new HashMap<OBJECT, Set<CLASS>>();
		for (final CLASS clazz : actual.getGroups()) {
			final HashSet<CLASS> cset = new HashSet<CLASS>();
			cset.add(clazz);
			for (final OBJECT instance : actual.getInstances(clazz)) {
				this.actual.put(instance, cset);
			}
		}
		this.analyser = analyser;
	}

	/**
	 * Construct a new {@link ClassificationEvaluator} with the given
	 * pre-classified results, the ground truth ("actual") data and an
	 * {@link ClassificationAnalyser}.
	 * <p>
	 * Internally, this constructor wraps a simple {@link Classifier}
	 * implementation around the results.
	 * 
	 * @param results
	 *            the pre-classified results
	 * @param actual
	 *            the ground truth
	 * @param analyser
	 *            the analyser
	 */
	public ClassificationEvaluator(final Map<OBJECT, ClassificationResult<CLASS>> results,
			Map<OBJECT, Set<CLASS>> actual, ClassificationAnalyser<RESULT, CLASS, OBJECT> analyser)
	{
		this.classifier = new Classifier<CLASS, OBJECT>() {
			@Override
			public ClassificationResult<CLASS> classify(OBJECT object) {
				return results.get(object);
			}
		};

		this.objects = actual.keySet();
		this.actual = actual;
		this.analyser = analyser;
	}

	@Override
	public Map<OBJECT, ClassificationResult<CLASS>> evaluate() {
		final Map<OBJECT, ClassificationResult<CLASS>> results = new HashMap<OBJECT, ClassificationResult<CLASS>>();

		for (final OBJECT object : objects) {
			results.put(object, classifier.classify(object));
		}

		return results;
	}

	@Override
	public RESULT analyse(Map<OBJECT, ClassificationResult<CLASS>> predicted) {
		return analyser.analyse(predicted, actual);
	}

	/**
	 * Get the expected classes for each instance
	 * 
	 * @return the map of instances to expected classes
	 */
	public Map<OBJECT, Set<CLASS>> getExpected() {
		return actual;
	}
}
