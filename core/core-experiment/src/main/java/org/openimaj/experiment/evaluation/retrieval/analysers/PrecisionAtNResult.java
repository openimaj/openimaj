package org.openimaj.experiment.evaluation.retrieval.analysers;

import gnu.trove.map.hash.TObjectDoubleHashMap;
import gnu.trove.procedure.TDoubleProcedure;
import gnu.trove.procedure.TObjectDoubleProcedure;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.openimaj.experiment.dataset.Identifiable;
import org.openimaj.experiment.evaluation.AnalysisResult;

import com.googlecode.jatl.Html;

/**
 * {@link AnalysisResult} used with {@link PrecisionAtN} to
 * hold the P@N precision after N documents have been retrieved.
 * <p>
 * Provides both per-query precisions and descriptive statistics
 * over all queries.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <QUERY> Type of query
 */
public class PrecisionAtNResult<QUERY> implements AnalysisResult {
	TObjectDoubleHashMap<QUERY> allScores = new TObjectDoubleHashMap<QUERY>();
	int N;

	/**
	 * Construct with the given N.
	 * @param N number of retrieved documents at which precision is calculated
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
	
	@Override
	public void writeHTML(File file, final String title, final String info) throws IOException {
		FileWriter fw = null;
		try {
			fw = new FileWriter(file);
			
			new Html(fw) {{
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
			}};
		} finally {
			if (fw != null) fw.close();
		}
	}
	
	@Override
	public String toString() {
		final StringBuilder outBuffer = new StringBuilder();
		
		allScores.forEachEntry(new TObjectDoubleProcedure<QUERY>() {
			@Override
			public boolean execute(QUERY a, double b) {
				String id;
				if (a instanceof Identifiable)
					id = ((Identifiable)a).getID();
				else 
					id = a.toString();
				
				outBuffer.append(String.format("P@%-11s\t%10s\t%6.4f\n", N, id, b));
				
				return true;
			}
		});
		outBuffer.append("\n");
		
		DescriptiveStatistics ds = computeStats();
		outBuffer.append("Aggregate P@"+N+" Statistics:\n");
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
}
