package org.openimaj.workinprogress.optimisation;

public interface ObjectiveFunction<MODEL, DATATYPE, KEY> {
	public abstract double value(MODEL model, DATATYPE data);

}
