package org.openimaj.math.model;

import gov.sandia.cognition.learning.algorithm.bayes.VectorNaiveBayesCategorizer;
import gov.sandia.cognition.learning.data.DefaultInputOutputPair;
import gov.sandia.cognition.learning.data.InputOutputPair;
import gov.sandia.cognition.math.matrix.Vector;
import gov.sandia.cognition.math.matrix.VectorFactory;
import gov.sandia.cognition.statistics.distribution.UnivariateGaussian.PDF;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.util.pair.IndependentPair;

public class GaussianVectorNaiveBayesModel<T> implements Model<double[], T> {
	VectorNaiveBayesCategorizer.BatchGaussianLearner<T> learner = new VectorNaiveBayesCategorizer.BatchGaussianLearner<T>();
	private VectorNaiveBayesCategorizer<T, PDF> model;
	
	@Override
	public void estimate(List<? extends IndependentPair<double[], T>> data) {
		List<InputOutputPair<Vector,T>> cfdata = new ArrayList<InputOutputPair<Vector,T>>();
		
		for (IndependentPair<double[],T> d : data) {
			InputOutputPair<Vector,T> iop = new DefaultInputOutputPair<Vector,T>(VectorFactory.getDefault().copyArray(d.firstObject()) , d.secondObject());
			cfdata.add(iop);
		}
		
		model = learner.learn(cfdata);
	}

	@Override
	public boolean validate(IndependentPair<double[], T> data) {
		return predict(data.firstObject()).equals(data.secondObject());
	}

	@Override
	public T predict(double[] data) {
		return model.evaluate(VectorFactory.getDefault().copyArray(data));
	}

	@Override
	public int numItemsToEstimate() {
		return 0;
	}

	@Override
	public double calculateError(List<? extends IndependentPair<double[], T>> data) {
		int count = 0;
		
		for (IndependentPair<double[], T> d : data) {
			if (!validate(d))
				count++;
		}
		
		return (double)count / (double)data.size();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public GaussianVectorNaiveBayesModel<T> clone() {
		try {
			return (GaussianVectorNaiveBayesModel<T>) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void main(String[] args) {
		GaussianVectorNaiveBayesModel<Boolean> model = new GaussianVectorNaiveBayesModel<Boolean>();
		
		List<IndependentPair<double[], Boolean>> data = new ArrayList<IndependentPair<double[], Boolean>>();
		
		data.add(IndependentPair.pair(new double[]{0}, true));
		data.add(IndependentPair.pair(new double[]{0.1}, true));
		data.add(IndependentPair.pair(new double[]{-0.1}, true));
		
		data.add(IndependentPair.pair(new double[]{9.9}, false));
		data.add(IndependentPair.pair(new double[]{10}, false));
		data.add(IndependentPair.pair(new double[]{10.1}, false));
		
		model.estimate(data);
		
		System.out.println(model.predict(new double[] {5.1}));
		
		
		System.out.println(model.model.getPriors());
	}
}
