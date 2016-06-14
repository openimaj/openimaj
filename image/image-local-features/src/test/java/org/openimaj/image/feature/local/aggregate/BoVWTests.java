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
