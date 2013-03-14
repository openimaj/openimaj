package org.openimaj.examples.image.feature.local;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.quantised.QuantisedLocalFeature;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.feature.local.aggregate.BagOfVisualWords;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.feature.local.keypoints.KeypointLocation;
import org.openimaj.ml.clustering.ByteCentroidsResult;
import org.openimaj.ml.clustering.assignment.HardAssigner;
import org.openimaj.ml.clustering.kmeans.ByteKMeans;

/**
 * 
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class BoVWExample {
	/**
	 * Main method
	 * 
	 * @param args
	 *            ignored
	 * @throws IOException
	 *             if the image can't be read
	 */
	public static void main(String[] args) throws IOException {
		//load an image
		final MBFImage image = ImageUtilities.readMBF(new URL(
				"http://users.ecs.soton.ac.uk/dpd/projects/openimaj/tutorial/hist1.jpg"));

		// Extract some local features; in this case, we'll get SIFT features
		// located at the extrema in the difference-of-Gaussian pyramid.
		final DoGSIFTEngine engine = new DoGSIFTEngine();
		final LocalFeatureList<Keypoint> imageKeypoints = engine.findFeatures(image.flatten());

		//Build 
		final byte[][] AllFeaturesDescriptors = new byte[imageKeypoints.size()][];

		for (int i = 0; i < imageKeypoints.size(); i++) {
			AllFeaturesDescriptors[i] = imageKeypoints.get(i).ivec;
		}

		/**
		 * clustering
		 */

		final ByteKMeans byteKmeans = ByteKMeans.createKDTreeEnsemble(128, 300);

		final ByteCentroidsResult result = byteKmeans.cluster(AllFeaturesDescriptors);

		/**
		 * hard assignement
		 */
		final HardAssigner<byte[], ?, ?> assigner = result.defaultHardAssigner();

		/**
		 * BagOfVisualWords construction!
		 */
		final BagOfVisualWords<byte[]> bagOfVisualWords = new BagOfVisualWords<byte[]>(assigner);

		final List<QuantisedLocalFeature<KeypointLocation>> quantizationResult =
				BagOfVisualWords.computeQuantisedFeatures(assigner, imageKeypoints);
	}
}
