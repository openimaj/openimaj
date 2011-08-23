package org.openimaj.image.annotation.xform;

import gnu.trove.TIntIntHashMap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.openimaj.image.annotation.AutoAnnotation;
import org.openimaj.image.annotation.BatchAnnotator;
import org.openimaj.image.annotation.ImageFeatureAnnotationProvider;
import org.openimaj.image.annotation.ImageFeatureProvider;

import cern.jet.random.Empirical;
import cern.jet.random.EmpiricalWalker;
import cern.jet.random.Uniform;
import cern.jet.random.engine.MersenneTwister;

public class UniformRandomAnnotator implements BatchAnnotator<Object> {
	List<String> annotations;
	EmpiricalWalker numAnnotations;
	Uniform rnd;
	
	@Override
	public List<AutoAnnotation> annotate(ImageFeatureProvider<Object> provider) {
		int nAnnotations = numAnnotations.nextInt();
		
		List<AutoAnnotation> annos = new ArrayList<AutoAnnotation>();
		
		for (int i=0; i<nAnnotations; i++) {
			int annotationIdx = rnd.nextInt();
			annos.add(new AutoAnnotation(annotations.get(annotationIdx), 1.0f/annotations.size()));
		}
		
		return annos;
	}

	@Override
	public void train(List<ImageFeatureAnnotationProvider<Object>> data) {
		HashSet<String> annotationsSet = new HashSet<String>();
		TIntIntHashMap nAnnotationCounts = new TIntIntHashMap();
		int maxVal = 0;
		
		for (ImageFeatureAnnotationProvider<Object> sample : data) {
			List<String> annos = sample.getAnnotations();
			annotationsSet.addAll(annos);
			nAnnotationCounts.adjustOrPutValue(annos.size(), 1, 1);
			
			if (annos.size()>maxVal) maxVal = annos.size();
		}
		
		annotations = new ArrayList<String>(annotationsSet);
		
		double [] distr = new double[maxVal+1];
		for (int i=0; i<=maxVal; i++) 
			distr[i] = nAnnotationCounts.get(i);
		
		numAnnotations = new EmpiricalWalker(distr, Empirical.NO_INTERPOLATION, new MersenneTwister());
		rnd = new Uniform(0, annotations.size()-1, new MersenneTwister());
	}
}
