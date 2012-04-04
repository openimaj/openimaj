package org.openimaj.experiment.evaluation.classification;

import org.openimaj.experiment.dataset.Identifiable;

public interface Classifier<CLASS extends Identifiable, OBJECT> {
	/**
	 * @param object
	 * @return
	 */
	public ClassificationResult<CLASS> classify(OBJECT object);
}
