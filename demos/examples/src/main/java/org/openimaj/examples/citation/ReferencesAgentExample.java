package org.openimaj.examples.citation;

import org.openimaj.OpenIMAJ;
import org.openimaj.citation.CitationAgent;
import org.openimaj.citation.ReferenceListener;
import org.openimaj.citation.annotation.output.StandardFormatters;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;

/**
 * Example showing how to load an class-transforming agent to augment a program
 * at runtime in order to extract references. See the <a
 * href="http://blog.openimaj.org/2012/08/28/adding-references-to-code/"
 * >blog-post</a> for more details.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class ReferencesAgentExample {
	/**
	 * Main method
	 * 
	 * @param args
	 *            ignored
	 * @throws Throwable
	 *             if an error occurs
	 */
	public static void main(String[] args) throws Throwable {
		// load the agent before doing anything else.
		CitationAgent.initialise();

		// now we run some code that contains references
		final FImage image = ImageUtilities.readF(OpenIMAJ.getLogoAsStream());

		final DoGSIFTEngine engine = new DoGSIFTEngine();
		for (final Keypoint keypoint : engine.findFeatures(image)) {
			System.out.println(keypoint);
		}

		// and when we're done we print the collected citations
		System.out.println(StandardFormatters.STRING.format(ReferenceListener.getReferences()));
	}
}
