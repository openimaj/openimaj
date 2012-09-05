package org.openimaj.examples.citation;

import org.openimaj.OpenIMAJ;
import org.openimaj.aop.classloader.ClassLoaderTransform;
import org.openimaj.citation.ReferenceListener;
import org.openimaj.citation.ReferencesClassTransformer;
import org.openimaj.citation.annotation.output.StandardFormatters;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;

/**
 * Example showing how to use the byte-code translating classloader to augment a
 * program at runtime in order to extract references. See the <a
 * href="http://blog.openimaj.org/2012/08/28/adding-references-to-code/"
 * >blog-post</a> for more details.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class ReferencesClassloaderExample {
	/**
	 * Main method
	 * 
	 * @param args
	 *            ignored
	 * @throws Throwable
	 *             if an error occurs
	 */
	public static void main(String[] args) throws Throwable {
		// the classloader must be initialised before any other classes are used
		if (ClassLoaderTransform.run(ReferencesClassloaderExample.class, args, new ReferencesClassTransformer()))
			return;

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
