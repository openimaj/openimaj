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
package org.openimaj.ml.annotation.linear;

import gov.sandia.cognition.learning.algorithm.svm.PrimalEstimatedSubGradient;
import gov.sandia.cognition.learning.data.DefaultInputOutputPair;
import gov.sandia.cognition.learning.data.InputOutputPair;
import gov.sandia.cognition.learning.function.categorization.LinearBinaryCategorizer;
import gov.sandia.cognition.math.matrix.Vector;
import gov.sandia.cognition.math.matrix.VectorFactory;
import gov.sandia.cognition.math.matrix.Vectorizable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openimaj.feature.FeatureExtractor;
import org.openimaj.feature.FeatureVector;
import org.openimaj.ml.annotation.Annotated;
import org.openimaj.ml.annotation.Annotator;
import org.openimaj.ml.annotation.BatchAnnotator;
import org.openimaj.ml.annotation.ScoredAnnotation;
import org.openimaj.ml.annotation.utils.AnnotatedListHelper;

/**
 * An {@link Annotator} based on a set of linear SVMs (one per annotation).
 * <p>
 * The SVMs use the PEGASOS algorithm implemented by the
 * {@link PrimalEstimatedSubGradient} class.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <OBJECT>
 *            Type of object being annotated
 * @param <ANNOTATION>
 *            Type of annotation
 */
public class LinearSVMAnnotator<OBJECT, ANNOTATION>
extends
BatchAnnotator<OBJECT, ANNOTATION>
{
	private final Map<ANNOTATION, LinearBinaryCategorizer> classifiers = new HashMap<ANNOTATION, LinearBinaryCategorizer>();
	private Set<ANNOTATION> annotations;
	private ANNOTATION negativeClass;
	private FeatureExtractor<? extends FeatureVector, OBJECT> extractor;

	/**
	 * Construct a new {@link LinearSVMAnnotator} with the given extractor and
	 * the specified negative class. The negative class is excluded from the
	 * predicted annotations.
	 *
	 * @param extractor
	 *            the extractor
	 * @param negativeClass
	 *            the negative class to exclude from predictions
	 */
	public LinearSVMAnnotator(FeatureExtractor<? extends FeatureVector, OBJECT> extractor, ANNOTATION negativeClass) {
		this.extractor = extractor;
		this.negativeClass = negativeClass;
	}

	/**
	 * Construct a new {@link LinearSVMAnnotator} with the given extractor.
	 *
	 * @param extractor
	 *            the extractor
	 */
	public LinearSVMAnnotator(FeatureExtractor<? extends FeatureVector, OBJECT> extractor) {
		this(extractor, null);
	}

	@Override
	public void train(List<? extends Annotated<OBJECT, ANNOTATION>> data) {
		final AnnotatedListHelper<OBJECT, ANNOTATION> helper = new AnnotatedListHelper<OBJECT, ANNOTATION>(data);

		annotations = helper.getAnnotations();

		for (final ANNOTATION annotation : annotations) {
			final PrimalEstimatedSubGradient pegasos = new PrimalEstimatedSubGradient();

			final List<? extends FeatureVector> positive = helper.extractFeatures(annotation,
					(FeatureExtractor<? extends FeatureVector, OBJECT>) extractor);
			final List<? extends FeatureVector> negative = helper.extractFeaturesExclude(annotation,
					(FeatureExtractor<? extends FeatureVector, OBJECT>) extractor);

			pegasos.learn(convert(positive, negative));
			classifiers.put(annotation, pegasos.getResult());
		}
	}

	private Collection<? extends InputOutputPair<? extends Vectorizable, Boolean>>
	convert(List<? extends FeatureVector> positive, List<? extends FeatureVector> negative)
	{
		final Collection<InputOutputPair<Vectorizable, Boolean>> data =
				new ArrayList<InputOutputPair<Vectorizable, Boolean>>(positive.size() + negative.size());

		for (final FeatureVector p : positive) {
			data.add(new DefaultInputOutputPair<Vectorizable, Boolean>(convert(p), true));
		}
		for (final FeatureVector n : negative) {
			data.add(new DefaultInputOutputPair<Vectorizable, Boolean>(convert(n), false));
		}

		return data;
	}

	@Override
	public Set<ANNOTATION> getAnnotations() {
		return annotations;
	}

	@Override
	public List<ScoredAnnotation<ANNOTATION>> annotate(OBJECT object) {
		final List<ScoredAnnotation<ANNOTATION>> results = new ArrayList<ScoredAnnotation<ANNOTATION>>();

		for (final ANNOTATION annotation : annotations) {
			// skip the negative class
			if (annotation.equals(negativeClass))
				continue;

			final FeatureVector feature = extractor.extractFeature(object);
			final Vector vector = convert(feature);

			final double result = classifiers.get(annotation).evaluateAsDouble(vector);

			if (result > 0) {
				results.add(new ScoredAnnotation<ANNOTATION>(annotation, (float) Math.abs(result)));
			}
		}

		return results;
	}

	private Vector convert(FeatureVector feature) {
		return VectorFactory.getDenseDefault().copyArray(feature.asDoubleVector());
	}
}
