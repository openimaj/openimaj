package org.openimaj.workinprogress.optimisation;

import org.openimaj.workinprogress.optimisation.params.Parameters;

public interface DifferentiableObjectiveFunction<MODEL, DATATYPE, PTYPE extends Parameters<PTYPE>>
extends
		ObjectiveFunction<MODEL, DATATYPE, PTYPE>
{
	public abstract PTYPE derivative(MODEL model, DATATYPE data);

	public abstract void updateModel(MODEL model, PTYPE weights);
}
