package org.openimaj.image.feature.local.aggregate;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.OpenIMAJ;
import org.openimaj.data.DataSource;
import org.openimaj.feature.SparseDoubleFV;
import org.openimaj.feature.SparseIntFV;
import org.openimaj.feature.local.data.LocalFeatureListDataSource;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.ml.clustering.ByteCentroidsResult;
import org.openimaj.ml.clustering.assignment.soft.ByteKNNAssigner;
import org.openimaj.ml.clustering.kmeans.ByteKMeans;

/**
 * Basic tests for BoVW and Soft BoVW
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class BoVWTests {
	private LocalFeatureList<Keypoint> features;
	private ByteCentroidsResult centroids;

	/**
	 * Setup for the tests - load an image, extract features, learn codebook.
	 *
	 * @throws IOException
	 */
	@Before
	public void setup() throws IOException {
		final FImage image = ImageUtilities.readF(OpenIMAJ.getLogoAsStream());
		features = new DoGSIFTEngine().findFeatures(image);
		final DataSource<byte[]> datasource = new LocalFeatureListDataSource<Keypoint, byte[]>(features);
		final ByteKMeans km = ByteKMeans.createExact(10, 1);
		centroids = km.cluster(datasource);
	}

	/**
	 * Test BoVW
	 */
	@Test
	public void testBoVW() {
		final BagOfVisualWords<byte[]> bovw = new BagOfVisualWords<byte[]>(centroids.defaultHardAssigner());

		final SparseIntFV vector = bovw.aggregate(features);

		assertEquals(10, vector.length());
	}

	/**
	 * Test SoftBoVW
	 */
	@Test
	public void testSoftBoVW() {
		final SoftBagOfVisualWords<byte[], float[]> bovw = new SoftBagOfVisualWords<byte[], float[]>(new ByteKNNAssigner(
				centroids, false, 1));

		final SparseDoubleFV vector = bovw.aggregate(features);

		assertEquals(10, vector.length());
	}

	/**
	 * Test SoftBoVW
	 */
	@Test
	public void testSoftBoVW2() {
		final SoftBagOfVisualWords<byte[], float[]> bovw = new SoftBagOfVisualWords<byte[], float[]>(new ByteKNNAssigner(
				centroids, false, 5));

		final SparseDoubleFV vector = bovw.aggregate(features);

		assertEquals(10, vector.length());
	}
}
