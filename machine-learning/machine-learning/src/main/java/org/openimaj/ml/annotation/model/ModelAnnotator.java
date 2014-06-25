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
package org.openimaj.ml.annotation.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openimaj.feature.FeatureExtractor;
import org.openimaj.math.model.EstimatableModel;
import org.openimaj.ml.annotation.Annotated;
import org.openimaj.ml.annotation.BatchAnnotator;
import org.openimaj.ml.annotation.ScoredAnnotation;
import org.openimaj.util.pair.IndependentPair;

/**
 * An {@link BatchAnnotator} backed by a {@link EstimatableModel}. This only really makes
 * sense if the dependent variable of the model can take a set of discrete
 * values.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 * @param <OBJECT>
 *            Type of object being annotated
 * @param <ANNOTATION>
 *            Type of annotation
 * @param <FEATURE>
 *            Type of feature extracted by the extractor
 */
public class ModelAnnotator<OBJECT, ANNOTATION, FEATURE>
		extends
		BatchAnnotator<OBJECT, ANNOTATION>
{
	EstimatableModel<FEATURE, ANNOTATION> model;
	Set<ANNOTATION> annotations;
	private FeatureExtractor<FEATURE, OBJECT> extractor;

	/**
	 * Construct with the given parameters.
	 * 
	 * @param extractor
	 *            The feature extractor
	 * @param model
	 *            The model
	 * @param annotations
	 *            The set of annotations that the model can produce
	 */
	public ModelAnnotator(FeatureExtractor<FEATURE, OBJECT> extractor, EstimatableModel<FEATURE, ANNOTATION> model,
			Set<ANNOTATION> annotations)
	{
		this.extractor = extractor;
		this.model = model;
		this.annotations = annotations;
	}

	@Override
	public void train(List<? extends Annotated<OBJECT, ANNOTATION>> data) {
		final List<IndependentPair<FEATURE, ANNOTATION>> featureData = new ArrayList<IndependentPair<FEATURE, ANNOTATION>>();

		for (final Annotated<OBJECT, ANNOTATION> a : data) {
			final FEATURE f = extractor.extractFeature(a.getObject());

			for (final ANNOTATION ann : a.getAnnotations())
				featureData.add(IndependentPair.pair(f, ann));
		}

		model.estimate(featureData);
	}

	@Override
	public Set<ANNOTATION> getAnnotations() {
		return annotations;
	}

	@Override
	public List<ScoredAnnotation<ANNOTATION>> annotate(OBJECT object) {
		final FEATURE f = extractor.extractFeature(object);

		final List<ScoredAnnotation<ANNOTATION>> result = new ArrayList<ScoredAnnotation<ANNOTATION>>();
		result.add(new ScoredAnnotation<ANNOTATION>(model.predict(f), 1));

		return result;
	}
}
