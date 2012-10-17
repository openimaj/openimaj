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

import java.util.ArrayList;
import java.util.List;

import net.sf.jasperreports.engine.JasperPrint;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.openimaj.experiment.evaluation.AnalysisResult;

import com.bethecoder.ascii_table.ASCIITable;

/**
 * Aggregated confusion matrix results
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <CLASS> The type of the classes represented by the {@link CMResult}s
 */
public class AggregatedCMResult<CLASS> implements AnalysisResult {
	/**
	 * Aggregated accuracy and error rate.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	public static class AggregateStatistics {
		private double meanAccuracy;
		private double stddevAccuracy;
		
		private double meanErrorRate;
		private double stddevErrorRate;
	}
	
	protected List<CMResult<CLASS>> matrices;

	/**
	 * Default constructor
	 */
	public AggregatedCMResult() {
		this.matrices = new ArrayList<CMResult<CLASS>>();
	}
	
	/**
	 * Construct with a list of results
	 * @param results the results
	 */
	public AggregatedCMResult(List<CMResult<CLASS>> results) {
		this.matrices = results;
	}

	/**
	 * Compute the current aggregate statistics of the
	 * accumulated results. 
	 * 
	 * @return the current aggregate statistics
	 */
	public AggregateStatistics computeStatistics() {
		DescriptiveStatistics accuracy = new DescriptiveStatistics();
		DescriptiveStatistics errorRate = new DescriptiveStatistics();
		
		for (CMResult<CLASS> result : matrices) {
			ConfusionMatrix<CLASS> m = result.getMatrix();
			accuracy.addValue(m.getAccuracy());
			errorRate.addValue(m.getErrorRate());
		}
		
		AggregateStatistics s = new AggregateStatistics();
		s.meanAccuracy = accuracy.getMean();
		s.stddevAccuracy = accuracy.getStandardDeviation();
		
		s.meanErrorRate = errorRate.getMean();
		s.stddevErrorRate = errorRate.getStandardDeviation();
		
		return s;
	}
	
	@Override
	public String toString() {
		return getSummaryReport();
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
		AggregateStatistics summary = computeStatistics();
		
		String [] header = {"Value", "Mean", "Standard Deviation"};
		String [][] data = {
				{ "Accuracy", String.format("%2.3f", summary.meanAccuracy), String.format("%2.3f", summary.stddevAccuracy) },
				{ "Error Rate", String.format("%2.3f", summary.meanErrorRate), String.format("%2.3f", summary.stddevErrorRate) }
		};
		
		return ASCIITable.getInstance().getTable(header, data);
	}

	@Override
	public String getDetailReport() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("** Summary Report **\n");
		sb.append(getSummaryReport());
		sb.append("\n");
		sb.append("** Per Run Reports **\n");
		for (int i=0; i<this.matrices.size(); i++) {
			CMResult<CLASS> result = this.matrices.get(i);
			
			
			sb.append("***************************************************************\n");
			sb.append("* Run #"+i+"\n");
			sb.append("***************************************************************\n");
			sb.append(result.getDetailReport());
			sb.append("\n");
		}
		
		return sb.toString();
	}
}
