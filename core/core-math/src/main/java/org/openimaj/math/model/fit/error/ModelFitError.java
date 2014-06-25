package org.openimaj.math.model.fit.error;

import java.util.List;

import org.openimaj.math.model.Model;
import org.openimaj.util.pair.IndependentPair;

/**
 * Interface describing the computation of an error (the residuals) of a set of
 * (independent and dependent) data points with respect to a model.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <I>
 *            type of independent data
 * @param <D>
 *            type of dependent data
 * @param <M>
 *            type of model
 */
public interface ModelFitError<I, D, M extends Model<I, D>> {
	/**
	 * Set the current model being evaluated. This should be called every time
	 * the model has changed internally, as the {@link ModelFitError} might
	 * pre-cache variables based on the model for error computation.
	 * 
	 * @param model
	 *            the model
	 */
	public void setModel(M model);

	/**
	 * Compute the error for a single point
	 * 
	 * @param data
	 *            the data
	 * @return the error
	 */
	public double computeError(IndependentPair<I, D> data);

	/**
	 * Compute the error for a set of data points
	 * 
	 * @param data
	 *            the data
	 * @param errors
	 *            the array to write the errors into
	 */
	public void computeError(List<? extends IndependentPair<I, D>> data, double[] errors);
}
