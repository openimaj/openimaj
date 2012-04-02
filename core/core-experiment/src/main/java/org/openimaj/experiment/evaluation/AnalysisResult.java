package org.openimaj.experiment.evaluation;

import java.io.File;
import java.io.IOException;

/**
 * The result of the analysis of raw data. All {@link AnalysisResult}s
 * are capable of being output to an html document, or converted
 * to a human readable {@link String}.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public interface AnalysisResult {
	/**
	 * Write the result to an html file (or files). The title and
	 * info arguments should be used to enrich the generated html.
	 * 
	 * The info argument may contain either an html fragment or
	 * a plain {@link String}. The title should only be a plain 
	 * {@link String}.
	 * 
	 * @param file the file to write to
	 * @param title the title of the evaluation
	 * @param info additional info about how the results were generated
	 * @throws IOException if there is a problem writing
	 */
	public void writeHTML(File file, String title, String info) throws IOException;
	
	@Override
	public String toString();
}
