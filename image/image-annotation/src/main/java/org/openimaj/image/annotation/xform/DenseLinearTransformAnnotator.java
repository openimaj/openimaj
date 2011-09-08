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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openimaj.feature.FeatureVector;
import org.openimaj.image.annotation.AutoAnnotation;
import org.openimaj.image.annotation.BatchAnnotator;
import org.openimaj.image.annotation.ImageFeatureAnnotationProvider;
import org.openimaj.image.annotation.ImageFeatureProvider;

import Jama.Matrix;

public class DenseLinearTransformAnnotator<T extends FeatureVector> implements BatchAnnotator<T> {
	List<String> terms;
	Matrix transform;
	int k = 10;
	
	@Override
	public List<AutoAnnotation> annotate(ImageFeatureProvider<T> data) {
		int featureLen = data.getFeature().length();
		
		Matrix F = new Matrix(featureLen, 1);
			
		double[] fv = data.getFeature().asDoubleVector();
		for (int j=0; j<featureLen; j++)
			F.getArray()[j][0] = fv[0];
		
		Matrix res = F.transpose().times(transform.transpose());
		
		List<AutoAnnotation> ann = new ArrayList<AutoAnnotation>();
		for (int i=0; i<terms.size(); i++) {
			ann.add( new AutoAnnotation(terms.get(i), (float) res.get(0,i)) );
		}
		
		Collections.sort(ann, new Comparator<AutoAnnotation>() {
			@Override
			public int compare(AutoAnnotation o1, AutoAnnotation o2) {
				return o1.confidence < o2.confidence ? 1 : -1;
			}
		});
		
		return ann;
	}

	@Override
	public void train(List<ImageFeatureAnnotationProvider<T>> data) {
		Set<String> termsSet = new HashSet<String>();
		
		for (ImageFeatureAnnotationProvider<T> d : data) 
			termsSet.addAll(d.getAnnotations());
		terms = new ArrayList<String>(termsSet);
		
		int termLen = terms.size();
		int featureLen = data.get(0).getFeature().length();
		int trainingLen = data.size();
		
		Matrix F = new Matrix(featureLen, trainingLen);
		Matrix W = new Matrix(termLen, trainingLen);
		
		for (int i=0; i<trainingLen; i++) { 
			ImageFeatureAnnotationProvider<T> d = data.get(i);
			
			double[] fv = d.getFeature().asDoubleVector();
			for (int j=0; j<featureLen; j++)
				F.getArray()[j][i] = fv[j];
			
			for (String t : d.getAnnotations()) {
				W.getArray()[terms.indexOf(t)][i]++;
			}
		}
		
		Matrix pinvF = lossyPseudoInverse(F, k);
		transform = pinvF.transpose().times(W.transpose()).transpose();
	}
	
	static Matrix lossyPseudoInverse(Matrix m, int k) {
		try {
			no.uib.cipr.matrix.DenseMatrix mjtA = new no.uib.cipr.matrix.DenseMatrix(m.getArray());
			no.uib.cipr.matrix.SVD svd = no.uib.cipr.matrix.SVD.factorize(mjtA);

			if (k > svd.getS().length || k<0) k = svd.getS().length;
			
			Matrix S = new Matrix(svd.getS().length, svd.getS().length);
			
			double[] Sarr = svd.getS();
			for (int i=0; i<k; i++) {
				S.set(i, i, 1.0 / Sarr[i]);  
			}
			
			for (int i=k; i<Sarr.length; i++) {
				S.set(i, i, 0);
			}
			
			Matrix Ut = new Matrix(svd.getU().numColumns(), svd.getU().numRows());
			for (int r=0; r<svd.getU().numRows(); r++)
				for (int c=0; c<svd.getU().numColumns(); c++)
					Ut.set(c, r, svd.getU().get(r, c));
			
			Matrix V = new Matrix(svd.getVt().numColumns(), svd.getS().length);
			for (int r=0; r<svd.getS().length; r++)
				for (int c=0; c<svd.getVt().numColumns(); c++)
					V.set(c, r, svd.getVt().get(r, c));
			
			return V.times(S).times(Ut);
		} catch (no.uib.cipr.matrix.NotConvergedException ex) {
			System.out.println(ex);
			return null;
		}
	}
	
	public static void main(String[] args) {
		Matrix m = new Matrix( new double[][] {
				{1, 2},
				{3, 4}
		});
		
		m.print(5, 5);
		
		lossyPseudoInverse(m, 10).print(5, 5);
	}
}
