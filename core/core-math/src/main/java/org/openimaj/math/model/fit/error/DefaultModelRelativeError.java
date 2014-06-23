package org.openimaj.math.model.fit.error;

import java.util.List;

import org.openimaj.math.model.Model;
import org.openimaj.util.pair.IndependentPair;

/**
 * Default implementation of a {@link ModelFitError} that uses
 * {@link Model#calculateError(List)} to compute the errors.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <I>
 *            type of independent data
 * @param <D>
 *            type of dependent data
 */
public class DefaultModelRelativeError<I, D> extends AbstractModelFitError<I, D> {
	@Override
	public double computeError(IndependentPair<I, D> data) {
		return model.calculateError(data);
	}
}
