package org.openimaj.experiment.evaluation.classification;

import java.util.Set;


/**
 * The result of a {@link Classifier}. The {@link ClassificationResult} models
 * one or more assigned classes together with associated confidence values for
 * each class.
 * <p>
 * Normally, confidence values are bounded between 0 and 1. 
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <CLASS> type of classes predicted
 */
public interface ClassificationResult<CLASS> {
	/**
	 * Get the confidence associated with the given class.
	 * If the class is unknown, then this method should
	 * return 0.
	 * <p>
	 * Normally, confidence values are bounded between 0 and 1.
	 * 
	 * @param clazz the target class.
	 * @return the confidence in assigning the class.
	 */
	public double getConfidence(CLASS clazz);
	
	/**
	 * Get the set of classes predicted by this result. Usually,
	 * this is a subset of all classes; specifically those with a 
	 * high confidence.
	 * 
	 * @return the set of predicted classes.
	 */
	public Set<CLASS> getPredictedClasses();
}
