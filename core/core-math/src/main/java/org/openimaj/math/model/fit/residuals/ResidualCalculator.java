package org.openimaj.math.model.fit.residuals;

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
public interface ResidualCalculator<I, D, M extends Model<I, D>> {
	/**
	 * Set the current model being evaluated. This should be called every time
	 * the model has changed internally, as the {@link ResidualCalculator} might
	 * pre-cache variables based on the model for error computation.
	 * 
	 * @param model
	 *            the model
	 */
	public void setModel(M model);

	/**
	 * Compute the residual for a single point
	 * 
	 * @param data
	 *            the data
	 * @return the error
	 */
	public double computeResidual(IndependentPair<I, D> data);

	/**
	 * Compute the residual for a set of data points
	 * 
	 * @param data
	 *            the data
	 * @param residuals
	 *            the array to write the residuals into
	 */
	public void computeResiduals(List<? extends IndependentPair<I, D>> data, double[] residuals);
}
