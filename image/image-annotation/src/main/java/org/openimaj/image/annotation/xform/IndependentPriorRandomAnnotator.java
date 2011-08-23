package org.openimaj.image.annotation.xform;

import gnu.trove.TDoubleArrayList;
import gnu.trove.TIntIntHashMap;
import gnu.trove.TObjectIntHashMap;
import gnu.trove.TObjectIntProcedure;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.annotation.AutoAnnotation;
import org.openimaj.image.annotation.BatchAnnotator;
import org.openimaj.image.annotation.ImageFeatureAnnotationProvider;
import org.openimaj.image.annotation.ImageFeatureProvider;

import cern.jet.random.Empirical;
import cern.jet.random.EmpiricalWalker;
import cern.jet.random.engine.MersenneTwister;

public class IndependentPriorRandomAnnotator implements BatchAnnotator<Object> {
	List<String> annotations;
	EmpiricalWalker numAnnotations;
	EmpiricalWalker annotationProbability;
	
	@Override
	public List<AutoAnnotation> annotate(ImageFeatureProvider<Object> provider) {
		int nAnnotations = numAnnotations.nextInt();
		
		List<AutoAnnotation> annos = new ArrayList<AutoAnnotation>();
		
		for (int i=0; i<nAnnotations; i++) {
			int annotationIdx = annotationProbability.nextInt();
			annos.add(new AutoAnnotation(annotations.get(annotationIdx), (float) annotationProbability.pdf(annotationIdx+1)));
		}
		
		return annos;
	}

	@Override
	public void train(List<ImageFeatureAnnotationProvider<Object>> data) {
		TIntIntHashMap nAnnotationCounts = new TIntIntHashMap();
		TObjectIntHashMap<String> annotationCounts = new TObjectIntHashMap<String>();
		int maxVal = 0;
		
		for (ImageFeatureAnnotationProvider<Object> sample : data) {
			List<String> annos = sample.getAnnotations();

			for (String s : annos) {
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
		annotations = new ArrayList<String>();
		final TDoubleArrayList probs = new TDoubleArrayList();
		annotationCounts.forEachEntry(new TObjectIntProcedure<String>() {
			@Override
			public boolean execute(String a, int b) {
				annotations.add(a);
				probs.add(b);
				return true;
			}
		});
		annotationProbability = new EmpiricalWalker(probs.toNativeArray(), Empirical.NO_INTERPOLATION, new MersenneTwister());
	}
}
