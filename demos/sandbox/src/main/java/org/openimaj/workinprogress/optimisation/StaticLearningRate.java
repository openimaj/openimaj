package org.openimaj.workinprogress.optimisation;

import org.openimaj.workinprogress.optimisation.params.Parameters;

public class StaticLearningRate<PTYPE extends Parameters<PTYPE>> extends LearningRate<PTYPE> {
	double rate;

	public StaticLearningRate(double rate) {
		this.rate = rate;
	}

	@Override
	public double getRate(int epoch, int iteration, PTYPE parameters) {
		return rate;
	}
}