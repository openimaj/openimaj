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
package org.openimaj.image.annotation.xform;

import gnu.trove.TDoubleArrayList;
import gnu.trove.TIntIntHashMap;
import gnu.trove.TObjectIntHashMap;
import gnu.trove.TObjectIntProcedure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openimaj.experiment.dataset.Dataset;
import org.openimaj.image.Image;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.annotation.AnnotatedImage;
import org.openimaj.image.annotation.AutoAnnotation;
import org.openimaj.image.annotation.BatchAnnotator;
import org.openimaj.util.pair.IndependentPair;

import cern.jet.random.Empirical;
import cern.jet.random.EmpiricalWalker;
import cern.jet.random.engine.MersenneTwister;

public class IndependentPriorRandomAnnotator<I extends Image<?, I>, A> extends BatchAnnotator<I, A, ImageAnalyser<I>> {
	List<A> annotations;
	EmpiricalWalker numAnnotations;
	EmpiricalWalker annotationProbability;
	
	public IndependentPriorRandomAnnotator() {
		super(null);
	}

	@Override
	public void train(Dataset<? extends AnnotatedImage<I, A>> data) {
		TIntIntHashMap nAnnotationCounts = new TIntIntHashMap();
		TObjectIntHashMap<A> annotationCounts = new TObjectIntHashMap<A>();
		int maxVal = 0;
		
		for (AnnotatedImage<I, A> sample : data) {
			Collection<A> annos = sample.getAnnotations();

			for (A s : annos) {
				annotationCounts.adjustOrPutValue(s, 1, 1);
			}

			nAnnotationCounts.adjustOrPutValue(annos.size(), 1, 1);
			
			if (annos.size()>maxVal) maxVal = annos.size();
		}
		
		//build distribution and rng for the number of annotations
		double [] distr = new double[maxVal+1];
		for (int i=0; i<=maxVal; i++) 
			distr[i] = nAnnotationCounts.get(i);
		numAnnotations = new EmpiricalWalker(distr, Empirical.NO_INTERPOLATION, new MersenneTwister());
		
		//build distribution and rng for each annotation
		annotations = new ArrayList<A>();
		final TDoubleArrayList probs = new TDoubleArrayList();
		annotationCounts.forEachEntry(new TObjectIntProcedure<A>() {
			@Override
			public boolean execute(A a, int b) {
				annotations.add(a);
				probs.add(b);
				return true;
			}
		});
		annotationProbability = new EmpiricalWalker(probs.toNativeArray(), Empirical.NO_INTERPOLATION, new MersenneTwister());
	}

	@Override
	public List<AutoAnnotation<A>> annotate(I image) {
		int nAnnotations = numAnnotations.nextInt();
		
		List<AutoAnnotation<A>> annos = new ArrayList<AutoAnnotation<A>>();
		
		for (int i=0; i<nAnnotations; i++) {
			int annotationIdx = annotationProbability.nextInt();
			annos.add(new AutoAnnotation(annotations.get(annotationIdx), (float) annotationProbability.pdf(annotationIdx+1)));
		}
		
		return annos;
	}
}
