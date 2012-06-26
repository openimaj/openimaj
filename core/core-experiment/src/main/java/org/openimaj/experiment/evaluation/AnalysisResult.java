package org.openimaj.experiment.evaluation;

import java.io.File;
import java.io.IOException;

/**
 * The result of the analysis of raw data. All {@link AnalysisResult}s
 * must be capable of being output to an HTML document, and converted
 * to a human readable {@link String}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public interface AnalysisResult {
	/**
	 * Write the result to an html file (or files). The title and
	 * info arguments should be used to enrich the generated html.
	 * <p>
	 * The info argument may contain either an html fragment or
	 * a plain {@link String}. The title should only be a plain 
	 * {@link String}.
	 * <p>
	 * If multiple HTML pages, or additional resources (i.e. images,
	 * javascript, css, etc) are produced, then by convention, these 
	 * should be placed in a folder alongside the main HTML page.
	 * The name of the directory should be the name of the HTML page
	 * (without the extension) appended with "_files".
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
