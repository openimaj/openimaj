package org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix;

import gov.sandia.cognition.learning.performance.categorization.ConfusionMatrix;

import java.io.File;
import java.io.IOException;

import org.openimaj.experiment.evaluation.AnalysisResult;

public class CMResult<CLASS> implements AnalysisResult {
	ConfusionMatrix<CLASS> matrix;
	
	public CMResult(ConfusionMatrix<CLASS> matrix) {
		this.matrix = matrix;
	}
	
	public ConfusionMatrix<CLASS> getMatrix() {
		return matrix;
	}

	@Override
	public void writeHTML(File file, String title, String info) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String toString() {
		return "Accuracy: " + matrix.getAccuracy();
	}
}
