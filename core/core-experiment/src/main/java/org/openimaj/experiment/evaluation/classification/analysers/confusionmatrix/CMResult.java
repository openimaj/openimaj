package org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix;

import gov.sandia.cognition.learning.performance.categorization.ConfusionMatrix;
import net.sf.jasperreports.engine.JasperPrint;

import org.openimaj.experiment.evaluation.AnalysisResult;

/**
 * Results of a confusion matrix analysis using the {@link CMAnalyser}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <CLASS> Type of classes in the confusion matrix
 */
public class CMResult<CLASS> implements AnalysisResult {
	ConfusionMatrix<CLASS> matrix;
	
	/**
	 * Construct with a {@link ConfusionMatrix}.
	 * @param matrix the matrix
	 */
	public CMResult(ConfusionMatrix<CLASS> matrix) {
		this.matrix = matrix;
	}
	
	/**
	 * Get the internal {@link ConfusionMatrix}.
	 * @return the confusion matrix
	 */
	public ConfusionMatrix<CLASS> getMatrix() {
		return matrix;
	}

	@Override
	public String toString() {
		return this.getSummaryReport();
	}

	@Override
	public JasperPrint getSummaryReport(String title, String info) {
		//FIXME:
		throw new UnsupportedOperationException();
	}

	@Override
	public JasperPrint getDetailReport(String title, String info) {
		//FIXME:
		throw new UnsupportedOperationException();
	}

	@Override
	public String getSummaryReport() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(String.format("%10s: %2.3f\n", "Accuracy", matrix.getAccuracy()));
		sb.append(String.format("%10s: %2.3f\n", "Error Rate", matrix.getErrorRate()));
		
		return sb.toString();
	}

	@Override
	public String getDetailReport() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("** Overall Results **\n");
		sb.append(String.format("%10s: %2.3f\n", "Total instances", matrix.getTotalCount()));
		sb.append(String.format("%10s: %2.3f\n", "Total correct", matrix.getTotalCorrectCount()));
		sb.append(String.format("%10s: %2.3f\n", "Total incorrect", matrix.getTotalIncorrectCount()));
		sb.append(String.format("%10s: %2.3f\n", "Accuracy", matrix.getAccuracy()));
		sb.append(String.format("%10s: %2.3f\n", "Error Rate", matrix.getErrorRate()));
		sb.append(String.format("%10s: %2.3f\n", "Average Class Accuracy", matrix.getAverageCategoryAccuracy()));
		sb.append(String.format("%10s: %2.3f\n", "Average Class Error Rate", matrix.getAverageCategoryErrorRate()));
		sb.append("\n");
		sb.append("** Per Class Results **\n");
		for (CLASS c : matrix.getActualCategories()) {
			sb.append(String.format("%10s: %s\n", "Class", c));
			sb.append(String.format("%10s: %2.3f\n", "Class Accuracy", matrix.getCategoryAccuracy(c)));
			sb.append(String.format("%10s: %2.3f\n", "Class Error Rate", matrix.getCategoryErrorRate(c)));
			sb.append("\n");
		}
		
		return sb.toString();
	}
}
