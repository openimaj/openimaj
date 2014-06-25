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
 * @param <M>
 *            type of model
 */
public abstract class AbstractModelFitError<I, D, M extends Model<I, D>> implements ModelFitError<I, D, M> {
	protected M model;

	@Override
	public void setModel(M model) {
		this.model = model;
	}

	@Override
	public void computeError(List<? extends IndependentPair<I, D>> data, double[] errors) {
		for (int i = 0; i < data.size(); i++) {
			errors[i] = this.computeError(data.get(i));
		}
	}
}
