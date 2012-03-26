package org.openimaj.ml.annotation.basic.util;

import org.openimaj.experiment.dataset.Dataset;
import org.openimaj.ml.annotation.Annotated;

/**
 * Choose how many annotations to produce.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public interface NumAnnotationsChooser {
	/**
	 * Train the chooser with the given dataset.
	 * @param <O> Type of object being annotated
	 * @param <A> Type of annotation
	 * @param data the training data
	 */
	public abstract <O, A> void train(Dataset<? extends Annotated<O, A>> data);

	/**
	 * @return the number of annotations to produce.
	 */
	public abstract int numAnnotations();
}
