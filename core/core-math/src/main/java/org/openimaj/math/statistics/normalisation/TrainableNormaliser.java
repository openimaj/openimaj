package org.openimaj.math.statistics.normalisation;

/**
 * Interface for {@link Normaliser}s that need to be pre-trained in order to
 * compute relevant statistics to perform the actual normalisation operation.
 * For example, to compute z-score's the training operation would compute the
 * mean and variance of the data.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public interface TrainableNormaliser extends Normaliser {
	/**
	 * Train the normaliser. This must be called before calling the
	 * {@link #normalise(double[])} or {@link #normalise(double[][])} methods.
	 * 
	 * @param data
	 *            the data to normalise.
	 */
	public abstract void train(double[][] data);
}
