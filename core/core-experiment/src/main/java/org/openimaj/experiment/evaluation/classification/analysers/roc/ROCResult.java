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
package org.openimaj.experiment.evaluation.classification.analysers.roc;

import gov.sandia.cognition.statistics.method.ReceiverOperatingCharacteristic;
import gov.sandia.cognition.statistics.method.ReceiverOperatingCharacteristic.DataPoint;
import gov.sandia.cognition.statistics.method.ReceiverOperatingCharacteristic.Statistic;

import java.util.Map;
import java.util.Map.Entry;

import net.sf.jasperreports.engine.JasperPrint;

import org.openimaj.experiment.evaluation.AnalysisResult;

/**
 * An {@link AnalysisResult} representing a set of ROC curves and associated
 * statistics.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <CLASS>
 *            Type of classes
 */
public class ROCResult<CLASS> implements AnalysisResult {
	Map<CLASS, ReceiverOperatingCharacteristic> rocData;

	/**
	 * Default constructor
	 * 
	 * @param rocData
	 */
	ROCResult(Map<CLASS, ReceiverOperatingCharacteristic> rocData) {
		this.rocData = rocData;
	}

	/**
	 * @return the {@link ReceiverOperatingCharacteristic} for each CLASS
	 */
	public Map<CLASS, ReceiverOperatingCharacteristic> getROCData() {
		return rocData;
	}

	@Override
	public String toString() {
		return getSummaryReport();
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

		for (final Entry<CLASS, ReceiverOperatingCharacteristic> entry : rocData.entrySet()) {
			final Statistic stats = entry.getValue().computeStatistics();

			sb.append(
					String.format("%10s\tAUC: %2.3f\tEER:%2.3f\n", entry.getKey(),
							stats.getAreaUnderCurve(), stats.getOptimalThreshold().getFalsePositiveRate())
					);
		}

		return sb.toString();
	}

	@Override
	public String getDetailReport() {
		final StringBuilder sb = new StringBuilder();

		for (final Entry<CLASS, ReceiverOperatingCharacteristic> entry : rocData.entrySet()) {
			final Statistic stats = entry.getValue().computeStatistics();

			sb.append("Class: " + entry.getKey() + "\n");

			sb.append(String.format("AUC: %2.3f\n", stats.getAreaUnderCurve()));
			sb.append(String.format("EER: %2.3f\n", stats.getOptimalThreshold().getFalsePositiveRate()));
			// sb.append(String.format(" D': %2.3f\n", stats.getDPrime()));

			sb.append("\n");
			sb.append("FPR\tTPR\n");
			for (final DataPoint dp : entry.getValue().getSortedROCData()) {
				sb.append(String.format("%2.3f\t%2.3f\n", dp.getFalsePositiveRate(), dp.getTruePositiveRate()));
			}
			sb.append("\n");
		}

		return sb.toString();
	}
}
