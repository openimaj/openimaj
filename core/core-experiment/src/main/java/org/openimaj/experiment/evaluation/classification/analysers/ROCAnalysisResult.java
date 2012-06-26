package org.openimaj.experiment.evaluation.classification.analysers;

import gov.sandia.cognition.statistics.method.ReceiverOperatingCharacteristic;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.openimaj.experiment.evaluation.AnalysisResult;

/**
 * An {@link AnalysisResult} representing a set of ROC curves and
 * associated statistics.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <CLASS> Type of classes
 */
public class ROCAnalysisResult<CLASS> implements AnalysisResult {
	Map<CLASS, ReceiverOperatingCharacteristic> rocData;
	
	/**
	 * Default constructor
	 * @param rocData
	 */
	ROCAnalysisResult(Map<CLASS, ReceiverOperatingCharacteristic> rocData) {
		this.rocData = rocData;
	}

	@Override
	public void writeHTML(File file, String title, String info) throws IOException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(); 
		
		for (Entry<CLASS, ReceiverOperatingCharacteristic> entry : rocData.entrySet()) {
			sb.append(entry.getKey().toString());
			sb.append(" AUC:");
			sb.append(entry.getValue().computeStatistics().getAreaUnderCurve());
			sb.append("\n");
			
//			for (DataPoint dp : entry.getValue().getSortedROCData()) {
//				sb.append("(" + dp.getFalsePositiveRate() + ", " + dp.getTruePositiveRate() + ")");
//			}
//			sb.append("\n");
		}
		
		return sb.toString();
	}
}
