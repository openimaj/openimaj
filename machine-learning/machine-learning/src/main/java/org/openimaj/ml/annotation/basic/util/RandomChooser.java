package org.openimaj.ml.annotation.basic.util;

import java.util.Random;

import org.openimaj.experiment.dataset.Dataset;
import org.openimaj.ml.annotation.Annotated;

/**
 * Choose a random number of annotations between the given
 * limits.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class RandomChooser implements NumAnnotationsChooser {
	private final Random rng = new Random();
	private int min;
	private int max;

	/**
	 * Construct so that the maximum possible number of annotations
	 * is max and the minimum is 0.
	 * @param max the maximum possible number of annotations
	 */
	public RandomChooser(int max) {
		this.max = max;
	}
	
	/**
	 * Construct so that the minimium possible number of annotations
	 * is min and the maximum is max.
	 * @param min the minimium possible number of annotations
	 * @param max the maximum possible number of annotations
	 */
	public RandomChooser(int min, int max) {
		this.min = min;
		this.max = max;
	}
	
	@Override
	public <O, A> void train(Dataset<? extends Annotated<O, A>> data) {
		//Do nothing
	}

	@Override
	public int numAnnotations() {
		return rng.nextInt(min + max) - min;
	}
}
