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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openimaj.experiment.dataset.Dataset;
import org.openimaj.feature.FeatureVector;
import org.openimaj.feature.FeatureVectorProvider;
import org.openimaj.image.Image;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.math.matrix.PseudoInverse;
import org.openimaj.ml.annotation.Annotated;
import org.openimaj.ml.annotation.AutoAnnotation;
import org.openimaj.ml.annotation.BatchAnnotator;
import org.openimaj.ml.annotation.FeatureExtractor;
import org.openimaj.util.pair.IndependentPair;

import Jama.Matrix;

public class DenseLinearTransformAnnotator<
	I extends Image<?, I>,
	A,
	E extends FeatureExtractor<? extends FeatureVector, I>>
extends
	BatchAnnotator<I, A, E> 
{
	List<A> terms;
	Matrix transform;
	int k = 10;
	
	public DenseLinearTransformAnnotator(E extractor) {
		super(extractor);
	}

	@Override
	public void train(Dataset<? extends Annotated<I, A>> data) {
		Set<A> termsSet = new HashSet<A>();
		
		for (Annotated<I, A> d : data) 
			termsSet.addAll(d.getAnnotations());
		terms = new ArrayList<A>(termsSet);
		
		final int termLen = terms.size();
		final int trainingLen = data.size();
		
		Annotated<I, A> first = data.getItem(0);
		double[] fv = extractor.extractFeature(first.getObject()).asDoubleVector();
		
		final int featureLen = fv.length;
		
		final Matrix F = new Matrix(trainingLen, featureLen);
		final Matrix W = new Matrix(trainingLen, termLen);
		
		addRow(F, W, 0, fv, first.getAnnotations());
		for (int i=1; i<trainingLen; i++) { 
			addRow(F, W, i, data.getItem(i));
		}
		
		Matrix pinvF = PseudoInverse.pseudoInverse(F, k);
		transform = pinvF.times(W);
	}

	private void addRow(Matrix F, Matrix W, int r, Annotated<I, A> data) {
		double[] fv = extractor.extractFeature(data.getObject()).asDoubleVector();
		
		addRow(F, W, r, fv, data.getAnnotations());
	}
	
	private void addRow(Matrix F, Matrix W, int r, double [] fv, Collection<A> annotations) {
		for (int j=0; j<F.getColumnDimension(); j++)
			F.getArray()[r][j] = fv[j];
		
		for (A t : annotations) {
			W.getArray()[r][terms.indexOf(t)]++;
		}
	}
	
	@Override
	public List<AutoAnnotation<A>> annotate(I image) {
		double[] fv = extractor.extractFeature(image).asDoubleVector();
		
		Matrix F = new Matrix(new double[][] {fv});
		
		Matrix res = F.times(transform);
		
		List<AutoAnnotation<A>> ann = new ArrayList<AutoAnnotation<A>>();
		for (int i=0; i<terms.size(); i++) {
			ann.add( new AutoAnnotation<A>(terms.get(i), (float) res.get(0,i)) );
		}
		
		Collections.sort(ann, new Comparator<AutoAnnotation<A>>() {
			@Override
			public int compare(AutoAnnotation<A> o1, AutoAnnotation<A> o2) {
				return o1.confidence < o2.confidence ? 1 : -1;
			}
		});
		
		return ann;
	}
}
