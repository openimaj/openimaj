package org.openimaj.ml.training;

import java.util.List;

/**
 * Interface describing objects capable of performing
 * training in "batch" mode; all training examples are 
 * presented at once. Calling the {@link #train(List)} 
 * method more than once will cause
 * the internal model to be re-initialised using the new
 * data. If you want to implement an object that 
 * can be updated, implement the {@link IncrementalTrainer}
 * interface instead. 
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <T> Type of object being trained upon
 */
public interface BatchTrainer<T> {
	/**
	 * Train the object with the given data. 
	 * {@link BatchTrainer}s should reset the underlying
	 * model everytime this method is called.
	 * 
	 * @param data the training data
	 */
	public abstract void train(List<? extends T> data);
}
