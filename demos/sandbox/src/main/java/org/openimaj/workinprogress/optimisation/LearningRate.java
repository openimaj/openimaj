package org.openimaj.workinprogress.optimisation;

import org.openimaj.workinprogress.optimisation.params.Parameters;

public abstract class LearningRate<PTYPE extends Parameters<PTYPE>> {
	public abstract double getRate(int epoch, int iteration, PTYPE parameters);
}