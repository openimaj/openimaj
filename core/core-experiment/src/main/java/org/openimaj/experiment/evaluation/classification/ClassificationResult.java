package org.openimaj.experiment.evaluation.classification;

import org.openimaj.experiment.dataset.Identifiable;

public interface ClassificationResult<CLASS extends Identifiable> {
	public CLASS getClassifiedClass();
	public double getConfidence();
}
