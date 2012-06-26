package org.openimaj.experiment.evaluation.retrieval.analysers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.openimaj.experiment.evaluation.AnalysisResult;

import com.googlecode.jatl.Html;

/**
 * An {@link AnalysisResult} wrapping the output of the
 * trec_eval tool.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class TRECResult implements AnalysisResult {
	String trecOutput;
	
	/**
	 * Construct with the given output from trec_eval
	 * @param trecOutput the output
	 */
	public TRECResult(String trecOutput) {
		this.trecOutput = trecOutput;
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
						pre().text(TRECResult.this.toString()).end();
				endAll();
			}};
		} finally {
			if (fw != null) fw.close();
		}
	}

	@Override
	public String toString() {
		return trecOutput;
	}
}
