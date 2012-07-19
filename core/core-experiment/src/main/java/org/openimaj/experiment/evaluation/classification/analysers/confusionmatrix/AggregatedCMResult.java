package org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix;

import gov.sandia.cognition.learning.performance.categorization.ConfusionMatrix;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.openimaj.experiment.evaluation.AnalysisResult;

public class AggregatedCMResult<CLASS> implements AnalysisResult {
	public static class Statistics {
		private double meanAccuracy;
		private double stddevAccuracy;
		
		@Override
		public String toString() {
			return "mean accuracy: " + meanAccuracy + ", stddev: " + stddevAccuracy;
		}
	}
	
	protected List<ConfusionMatrix<CLASS>> matrices;

	public AggregatedCMResult() {
		this.matrices = new ArrayList<ConfusionMatrix<CLASS>>();
	}
	
	public AggregatedCMResult(List<ConfusionMatrix<CLASS>> matrices) {
		this.matrices = matrices;
	}

	public Statistics computeStatistics() {
		DescriptiveStatistics accuracy = new DescriptiveStatistics();
		
		for (ConfusionMatrix<CLASS> m : matrices) {
			accuracy.addValue(m.getAccuracy());
		}
		
		Statistics s = new Statistics();
		s.meanAccuracy = accuracy.getMean();
		s.stddevAccuracy = accuracy.getStandardDeviation();
		
		return s;
	}
	
	@Override
	public void writeHTML(File file, String title, String info) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String toString() {
		return computeStatistics().toString();
	}
}
