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

import gnu.trove.map.hash.TIntIntHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openimaj.ml.annotation.Annotated;
import org.openimaj.ml.annotation.BatchAnnotator;
import org.openimaj.ml.annotation.ScoredAnnotation;
import org.openimaj.ml.annotation.basic.util.NumAnnotationsChooser;

import cern.jet.random.Uniform;
import cern.jet.random.engine.MersenneTwister;

/**
 * An annotator that chooses annotations completely randomly from the set of all
 * known annotations. The number of annotations produced is set by the type of
 * {@link NumAnnotationsChooser} used.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <OBJECT>
 *            Type of object being annotated
 * @param <ANNOTATION>
 *            Type of annotation.
 */
public class UniformRandomAnnotator<OBJECT, ANNOTATION> extends BatchAnnotator<OBJECT, ANNOTATION> {
	protected List<ANNOTATION> annotations;
	protected NumAnnotationsChooser numAnnotations;
	protected Uniform rnd;

	/**
	 * Construct with the given {@link NumAnnotationsChooser} to determine how
	 * many annotations are produced by calls to {@link #annotate(Object)}.
	 * 
	 * @param chooser
	 *            the {@link NumAnnotationsChooser} to use.
	 */
	public UniformRandomAnnotator(NumAnnotationsChooser chooser) {
		this.numAnnotations = chooser;
	}

	@Override
	public void train(List<? extends Annotated<OBJECT, ANNOTATION>> data) {
		final HashSet<ANNOTATION> annotationsSet = new HashSet<ANNOTATION>();
		final TIntIntHashMap nAnnotationCounts = new TIntIntHashMap();
		int maxVal = 0;

		for (final Annotated<OBJECT, ANNOTATION> sample : data) {
			final Collection<ANNOTATION> annos = sample.getAnnotations();
			annotationsSet.addAll(annos);
			nAnnotationCounts.adjustOrPutValue(annos.size(), 1, 1);

			if (annos.size() > maxVal)
				maxVal = annos.size();
		}

		annotations = new ArrayList<ANNOTATION>(annotationsSet);

		rnd = new Uniform(0, annotations.size() - 1, new MersenneTwister());

		numAnnotations.train(data);
	}

	@Override
	public List<ScoredAnnotation<ANNOTATION>> annotate(OBJECT image) {
		final int nAnnotations = numAnnotations.numAnnotations();

		final List<ScoredAnnotation<ANNOTATION>> annos = new ArrayList<ScoredAnnotation<ANNOTATION>>();

		for (int i = 0; i < nAnnotations; i++) {
			final int annotationIdx = rnd.nextInt();
			annos.add(new ScoredAnnotation<ANNOTATION>(annotations.get(annotationIdx), 1.0f / annotations.size()));
		}

		return annos;
	}

	@Override
	public Set<ANNOTATION> getAnnotations() {
		return new HashSet<ANNOTATION>(annotations);
	}
}
