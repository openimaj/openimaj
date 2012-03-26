package org.openimaj.ml.annotation.basic.util;

import gnu.trove.TIntIntHashMap;

import java.util.Collection;

import org.openimaj.experiment.dataset.Dataset;
import org.openimaj.ml.annotation.Annotated;

import cern.jet.random.Empirical;
import cern.jet.random.EmpiricalWalker;
import cern.jet.random.engine.MersenneTwister;

/**
 * Choose the number of annotations based on the numbers of annotations
 * of each training example. Internally this {@link NumAnnotationsChooser}
 * constructs the distribution of annotation lengths from the training
 * data and then picks lengths randomly based on the distribution.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 */
public class PriorChooser implements NumAnnotationsChooser {

	private EmpiricalWalker numAnnotations;

	@Override
	public <O, A> void train(Dataset<? extends Annotated<O, A>> data) {
		TIntIntHashMap nAnnotationCounts = new TIntIntHashMap();
		int maxVal = 0;
		
		for (Annotated<O, A> sample : data) {
			Collection<A> annos = sample.getAnnotations();

			nAnnotationCounts.adjustOrPutValue(annos.size(), 1, 1);
			
			if (annos.size()>maxVal) maxVal = annos.size();
		}
		
		//build distribution and rng for the number of annotations
		double [] distr = new double[maxVal+1];
		for (int i=0; i<=maxVal; i++) 
			distr[i] = nAnnotationCounts.get(i);
		numAnnotations = new EmpiricalWalker(distr, Empirical.NO_INTERPOLATION, new MersenneTwister());
	}

	@Override
	public int numAnnotations() {
		return numAnnotations.nextInt();
	}
}
