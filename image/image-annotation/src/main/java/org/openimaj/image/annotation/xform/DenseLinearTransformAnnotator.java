///**
// * Copyright (c) 2011, The University of Southampton and the individual contributors.
// * All rights reserved.
// *
// * Redistribution and use in source and binary forms, with or without modification,
// * are permitted provided that the following conditions are met:
// *
// *   * 	Redistributions of source code must retain the above copyright notice,
// * 	this list of conditions and the following disclaimer.
// *
// *   *	Redistributions in binary form must reproduce the above copyright notice,
// * 	this list of conditions and the following disclaimer in the documentation
// * 	and/or other materials provided with the distribution.
// *
// *   *	Neither the name of the University of Southampton nor the names of its
// * 	contributors may be used to endorse or promote products derived from this
// * 	software without specific prior written permission.
// *
// * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
// * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
// * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
// * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
// * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
// */
//package org.openimaj.image.annotation.xform;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//import org.openimaj.feature.FeatureVector;
//import org.openimaj.image.annotation.AutoAnnotation;
//import org.openimaj.image.annotation.BatchAnnotator;
//import org.openimaj.image.annotation.ImageFeatureAnnotationProvider;
//import org.openimaj.image.annotation.ImageFeatureProvider;
//import org.openimaj.math.matrix.PseudoInverse;
//
//import Jama.Matrix;
//
//public class DenseLinearTransformAnnotator<T extends FeatureVector> implements BatchAnnotator<T> {
//	List<String> terms;
//	Matrix transform;
//	int k = 10;
//	
//	@Override
//	public List<AutoAnnotation> annotate(ImageFeatureProvider<T> data) {		
////		int featureLen = data.getFeature().length();
////		Matrix F = new Matrix(featureLen, 1);
//		
//		double[] fv = data.getFeature().asDoubleVector();
////		for (int j=0; j<featureLen; j++) {
////			F.getArray()[j][0] = fv[j];
////		}
//		
//		Matrix F = new Matrix(new double[][] {fv});
//		
//		//Matrix res = F.transpose().times(transform.transpose());
//		Matrix res = F.times(transform);
//		
//		List<AutoAnnotation> ann = new ArrayList<AutoAnnotation>();
//		for (int i=0; i<terms.size(); i++) {
//			ann.add( new AutoAnnotation(terms.get(i), (float) res.get(0,i)) );
//		}
//		
//		Collections.sort(ann, new Comparator<AutoAnnotation>() {
//			@Override
//			public int compare(AutoAnnotation o1, AutoAnnotation o2) {
//				return o1.confidence < o2.confidence ? 1 : -1;
//			}
//		});
//		
//		return ann;
//	}
//
//	@Override
//	public void train(List<ImageFeatureAnnotationProvider<T>> data) {
//		Set<String> termsSet = new HashSet<String>();
//		
//		for (ImageFeatureAnnotationProvider<T> d : data) 
//			termsSet.addAll(d.getAnnotations());
//		terms = new ArrayList<String>(termsSet);
//		
//		final int termLen = terms.size();
//		final int featureLen = data.get(0).getFeature().length();
//		final int trainingLen = data.size();
//		
////		final Matrix F = new Matrix(featureLen, trainingLen);
////		final Matrix W = new Matrix(termLen, trainingLen);
//		final Matrix F = new Matrix(trainingLen, featureLen);
//		final Matrix W = new Matrix(trainingLen, termLen);
//		
//		for (int i=0; i<trainingLen; i++) { 
//			ImageFeatureAnnotationProvider<T> d = data.get(i);
//			
//			double[] fv = d.getFeature().asDoubleVector();
//			for (int j=0; j<featureLen; j++)
////				F.getArray()[j][i] = fv[j];
//				F.getArray()[i][j] = fv[j];
//			
//			for (String t : d.getAnnotations()) {
////				W.getArray()[terms.indexOf(t)][i]++;
//				W.getArray()[i][terms.indexOf(t)]++;
//			}
//		}
//		
//		Matrix pinvF = PseudoInverse.pseudoInverse(F, k);
////		transform = (pinvF.transpose().times(W.transpose())).transpose();
//		transform = pinvF.times(W);
//	}
//}
