package org.openimaj.ml.annotation.basic.util;

import org.openimaj.experiment.dataset.Dataset;
import org.openimaj.ml.annotation.Annotated;

/**
 * Always choose the same (fixed) number of annotations
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class FixedChooser implements NumAnnotationsChooser {
	int numAnnotations;
	
	/**
	 * Construct with the given number of annotations.
	 * @param numAnnotations the number of annotations.
	 */
	public FixedChooser(int numAnnotations) {
		this.numAnnotations = numAnnotations;
	}
	
	@Override
	public <O, A> void train(Dataset<? extends Annotated<O, A>> data) {
		//do nothing
	}

	@Override
	public int numAnnotations() {
		return numAnnotations;
	}

}
