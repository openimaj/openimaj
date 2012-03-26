package org.openimaj.experiment.evaluation;

public interface Evaluator<T, Q> {
	public T evaluate();
	public Q analyse(T rawData);
}
