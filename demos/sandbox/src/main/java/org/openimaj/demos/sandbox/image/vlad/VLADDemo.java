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
package org.openimaj.demos.sandbox.image.vlad;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openimaj.feature.MultidimensionalFloatFV;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.normalisation.HellingerNormaliser;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.feature.local.aggregate.VLAD;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.FloatKeypoint;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.indexing.vlad.VLADIndexerData;

public class VLADDemo {
	public static void main(String[] args) throws IOException {
		final FImage image = ImageUtilities.readF(new File("/Users/jsh2/Data/ukbench/full/ukbench01001.jpg"));
		final DoGSIFTEngine engine = new DoGSIFTEngine();
		engine.getOptions().setDoubleInitialImage(false);

		final LocalFeatureList<Keypoint> features = engine.findFeatures(image);
		final List<FloatKeypoint> fkeys = FloatKeypoint.convert(features);

		for (final FloatKeypoint k : fkeys)
			HellingerNormaliser.normalise(k.vector, 0);

		final VLADIndexerData indexer = VLADIndexerData.read(new File("/Users/jsh2/vlad-indexer-ukbench-2x.dat"));

		// final ByteCentroidsResult centroids = IOUtils.read(new
		// File("/Users/jsh2/Desktop/ukbench16.voc"),
		// ByteCentroidsResult.class);
		// final ExactByteAssigner assigner = new ExactByteAssigner(centroids);
		// final VLAD<byte[]> vlad = new VLAD<byte[]>(assigner,
		// centroids.centroids, true);
		// final MultidimensionalFloatFV agg = vlad.aggregate(features);

		final MultidimensionalFloatFV agg = indexer.getVLAD().aggregate(fkeys);

		System.out.println(agg);

		DisplayUtilities.display(VLAD.drawDescriptor(agg.values, 64, 4, 8));
	}
}
