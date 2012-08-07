package org.openimaj.experiment.evaluation.classification.analysers.roc;

import gov.sandia.cognition.statistics.method.ReceiverOperatingCharacteristic;
import gov.sandia.cognition.statistics.method.ReceiverOperatingCharacteristic.DataPoint;
import gov.sandia.cognition.statistics.method.ReceiverOperatingCharacteristic.Statistic;

import java.util.Map;
import java.util.Map.Entry;

import net.sf.jasperreports.engine.JasperPrint;

import org.openimaj.experiment.evaluation.AnalysisResult;

/**
 * An {@link AnalysisResult} representing a set of ROC curves and
 * associated statistics.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <CLASS> Type of classes
 */
public class ROCResult<CLASS> implements AnalysisResult {
	Map<CLASS, ReceiverOperatingCharacteristic> rocData;
	
	/**
	 * Default constructor
	 * @param rocData
	 */
	ROCResult(Map<CLASS, ReceiverOperatingCharacteristic> rocData) {
		this.rocData = rocData;
	}
	
	/**
	 * @return the {@link ReceiverOperatingCharacteristic} for each CLASS
	 */
	public Map<CLASS, ReceiverOperatingCharacteristic> getROCData(){
		return rocData;
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
		StringBuilder sb = new StringBuilder(); 
		
		for (Entry<CLASS, ReceiverOperatingCharacteristic> entry : rocData.entrySet()) {
			Statistic stats = entry.getValue().computeStatistics();
			
			sb.append(
				String.format("%10s\tAUC: %2.3f\tEER:%2.3f\tD': %2.3f\n", entry.getKey(), 
						stats.getAreaUnderCurve(), stats.getOptimalThreshold(), stats.getDPrime())
			);
		}
		
		return sb.toString();
	}

	@Override
	public String getDetailReport() {
		StringBuilder sb = new StringBuilder(); 
		
		for (Entry<CLASS, ReceiverOperatingCharacteristic> entry : rocData.entrySet()) {
			Statistic stats = entry.getValue().computeStatistics();
			
			sb.append("Class: " + entry.getKey() + "\n");
			
			sb.append(String.format("AUC: %2.3f\n", stats.getAreaUnderCurve()));
			//sb.append(String.format("EER: %2.3f\n", stats.getOptimalThreshold()));
			sb.append(String.format(" D': %2.3f\n", stats.getDPrime()));
			
			sb.append("\n");
			sb.append("FPR\tTPR\n");
			for (DataPoint dp : entry.getValue().getSortedROCData()) {
				sb.append(String.format("%2.3f\t%2.3f\n", dp.getFalsePositiveRate(), dp.getTruePositiveRate()));
			}
			sb.append("\n");
		}
		
		return sb.toString();
	}
}
