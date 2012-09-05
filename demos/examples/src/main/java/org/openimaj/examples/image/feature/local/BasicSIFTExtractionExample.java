package org.openimaj.examples.image.feature.local;

import java.io.IOException;

import org.openimaj.OpenIMAJ;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.io.IOUtils;

/**
 * Example showing how to extract SIFT features from an image and write them to
 * {@link System#out} in a format compatible with David Lowe's <a
 * href="http://www.cs.ubc.ca/~lowe/keypoints/">keypoint</a> tool.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class BasicSIFTExtractionExample {
	/**
	 * Main method
	 * 
	 * @param args
	 *            ignored
	 * @throws IOException
	 *             if the image can't be read
	 */
	public static void main(String[] args) throws IOException {
		// read an image
		final FImage image = ImageUtilities.readF(OpenIMAJ.getLogoAsStream());

		// create the extractor - this can be reused (and is thread-safe)
		final DoGSIFTEngine engine = new DoGSIFTEngine();

		// find interest points in the image and extract SIFT descriptors
		final LocalFeatureList<Keypoint> keypoints = engine.findFeatures(image);

		// print the interest points to stdout
		IOUtils.writeASCII(System.out, keypoints);
	}
}
