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
package org.openimaj.experiment.evaluation.retrieval.analysers;

import gnu.trove.map.hash.TObjectDoubleHashMap;
import gnu.trove.procedure.TDoubleProcedure;
import gnu.trove.procedure.TObjectDoubleProcedure;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.openimaj.data.identity.Identifiable;
import org.openimaj.experiment.evaluation.AnalysisResult;

import com.googlecode.jatl.Html;

/**
 * {@link AnalysisResult} used with {@link PrecisionAtN} to hold the P@N
 * precision after N documents have been retrieved.
 * <p>
 * Provides both per-query precisions and descriptive statistics over all
 * queries.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <QUERY>
 *            Type of query
 */
public class PrecisionAtNResult<QUERY> implements AnalysisResult {
	TObjectDoubleHashMap<QUERY> allScores = new TObjectDoubleHashMap<QUERY>();
	int N;

	/**
	 * Construct with the given N.
	 * 
	 * @param N
	 *            number of retrieved documents at which precision is calculated
	 */
	public PrecisionAtNResult(int N) {
		this.N = N;
	}

	private DescriptiveStatistics computeStats() {
		final DescriptiveStatistics ds = new DescriptiveStatistics();

		allScores.forEachValue(new TDoubleProcedure() {
			@Override
			public boolean execute(double value) {
				ds.addValue(value);
				return true;
			}
		});

		return ds;
	}

	void writeHTML(File file, final String title, final String info) throws IOException {
		FileWriter fw = null;
		try {
			fw = new FileWriter(file);

			new Html(fw) {
				{
					html();
					head();
					title(title);
					end();
					body();
					h1().text(title).end();
					div().text(info).end();
					hr();
					pre().text(PrecisionAtNResult.this.toString()).end();
					endAll();
				}
			};
		} finally {
			if (fw != null)
				fw.close();
		}
	}

	@Override
	public String toString() {
		return getSummaryReport();
	}

	@Override
	public JasperPrint getSummaryReport(String title, String info) throws JRException {
		// FIXME
		throw new UnsupportedOperationException();
	}

	@Override
	public JasperPrint getDetailReport(String title, String info) throws JRException {
		// FIXME
		throw new UnsupportedOperationException();
	}

	@Override
	public String getSummaryReport() {
		final StringBuilder outBuffer = new StringBuilder();

		final DescriptiveStatistics ds = computeStats();
		outBuffer.append("Aggregate P@" + N + " Statistics:\n");
		outBuffer.append(String.format("%-15s\t%6d\n", "num_q", ds.getN()));
		outBuffer.append(String.format("%-15s\t%6.4f\n", "min", ds.getMin()));
		outBuffer.append(String.format("%-15s\t%6.4f\n", "max", ds.getMax()));
		outBuffer.append(String.format("%-15s\t%6.4f\n", "mean", ds.getMean()));
		outBuffer.append(String.format("%-15s\t%6.4f\n", "std dev", ds.getStandardDeviation()));
		outBuffer.append(String.format("%-15s\t%6.4f\n", "median", ds.getPercentile(50)));
		outBuffer.append(String.format("%-15s\t%6.4f\n", "skewness", ds.getSkewness()));
		outBuffer.append(String.format("%-15s\t%6.4f\n", "kurtosis", ds.getKurtosis()));

		return outBuffer.toString();
	}

	@Override
	public String getDetailReport() {
		final StringBuilder outBuffer = new StringBuilder();

		allScores.forEachEntry(new TObjectDoubleProcedure<QUERY>() {
			@Override
			public boolean execute(QUERY a, double b) {
				String id;
				if (a instanceof Identifiable)
					id = ((Identifiable) a).getID();
				else
					id = a.toString();

				outBuffer.append(String.format("P@%-11s\t%10s\t%6.4f\n", N, id, b));

				return true;
			}
		});
		outBuffer.append("\n");

		outBuffer.append(getSummaryReport());

		return outBuffer.toString();
	}
}
