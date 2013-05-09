/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix;

import gov.sandia.cognition.learning.performance.categorization.ConfusionMatrix;
import net.sf.jasperreports.engine.JasperPrint;

import org.openimaj.experiment.evaluation.AnalysisResult;

/**
 * Results of a confusion matrix analysis using the {@link CMAnalyser}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <CLASS>
 *            Type of classes in the confusion matrix
 */
public class CMResult<CLASS> implements AnalysisResult {
	ConfusionMatrix<CLASS> matrix;

	/**
	 * Construct with a {@link ConfusionMatrix}.
	 * 
	 * @param matrix
	 *            the matrix
	 */
	public CMResult(ConfusionMatrix<CLASS> matrix) {
		this.matrix = matrix;
	}

	/**
	 * Get the internal {@link ConfusionMatrix}.
	 * 
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
		// FIXME:
		throw new UnsupportedOperationException();
	}

	@Override
	public JasperPrint getDetailReport(String title, String info) {
		// FIXME:
		throw new UnsupportedOperationException();
	}

	@Override
	public String getSummaryReport() {
		final StringBuilder sb = new StringBuilder();

		sb.append(String.format("%10s: %2.3f\n", "Accuracy", matrix.getAccuracy()));
		sb.append(String.format("%10s: %2.3f\n", "Error Rate", matrix.getErrorRate()));

		return sb.toString();
	}

	@Override
	public String getDetailReport() {
		final StringBuilder sb = new StringBuilder();

		sb.append("*********************** Overall Results ***********************\n");
		sb.append(String.format("%25s: %2.3f\n", "Total instances", matrix.getTotalCount()));
		sb.append(String.format("%25s: %2.3f\n", "Total correct", matrix.getTotalCorrectCount()));
		sb.append(String.format("%25s: %2.3f\n", "Total incorrect", matrix.getTotalIncorrectCount()));
		sb.append(String.format("%25s: %2.3f\n", "Accuracy", matrix.getAccuracy()));
		sb.append(String.format("%25s: %2.3f\n", "Error Rate", matrix.getErrorRate()));
		sb.append(String.format("%25s: %2.3f\n", "Average Class Accuracy", matrix.getAverageCategoryAccuracy()));
		sb.append(String.format("%25s: %2.3f\n", "Average Class Error Rate", matrix.getAverageCategoryErrorRate()));
		sb.append("\n");
		sb.append("********************** Per Class Results **********************\n");
		sb.append(String.format("%s\t", "Class"));
		sb.append(String.format("%s\t", "Class Accuracy"));
		sb.append(String.format("%s\t", "Class Error Rate"));
		sb.append(String.format("%s\t", "Actual Count"));
		sb.append(String.format("%s\n", "Predicted Count"));
		for (final CLASS c : matrix.getActualCategories()) {
			sb.append(String.format("%10s\t", c));
			sb.append(String.format("%2.3f\t", matrix.getCategoryAccuracy(c)));
			sb.append(String.format("%2.3f\t", matrix.getCategoryErrorRate(c)));
			sb.append(String.format("%6f\t", matrix.getActualCount(c)));
			sb.append(String.format("%6f\n", matrix.getPredictedCount(c)));
		}

		return sb.toString();
	}
}
