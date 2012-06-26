package org.openimaj.experiment.evaluation.classification;


/**
 * Interface describing a classifier. The {@link Classifier} class
 * aims to be general enough to support binary classifiers, multi-label 
 * classifiers or even multi-class classifiers.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <CLASS> Type of classes
 * @param <OBJECT> Type of objects
 */
public interface Classifier<CLASS, OBJECT> {
	/**
	 * Classify an object.
	 * 
	 * @param object the object to classify.
	 * @return classes and scores for the object.
	 */
	public ClassificationResult<CLASS> classify(OBJECT object);
}
