package org.openimaj.ml.training;

/**
 * Interface describing objects capable of performing
 * incremental training.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <T> Type being trained on
 */
public interface IncrementalTrainer<T> {
	/**
	 * Train the object with the given data.
	 * 
	 * @param data the training data
	 */
	public void train(Iterable<? extends T> data);

	/**
	 * Train/update object using a new instance.
	 * @param annotated instance to train with
	 */
	public abstract void train(T annotated);
	
	/**
	 * Reset the object to its initial condition, as if
	 * it hasn't seen any training data.
	 */
	public abstract void reset();
}
