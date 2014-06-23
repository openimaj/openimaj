package org.openimaj.math.model.fit.error;

import java.util.List;

import org.openimaj.math.model.Model;
import org.openimaj.util.pair.IndependentPair;

/**
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <I>
 *            type of independent data
 * @param <D>
 *            type of dependent data
 */
public abstract class AbstractModelFitError<I, D> implements ModelFitError<I, D> {
	protected Model<I, D> model;

	@Override
	public void setModel(Model<I, D> model) {
		this.model = model;
	}

	@Override
	public void computeError(List<? extends IndependentPair<I, D>> data, double[] errors) {
		for (int i = 0; i < data.size(); i++) {
			errors[i] = this.computeError(data.get(i));
		}
	}
}
