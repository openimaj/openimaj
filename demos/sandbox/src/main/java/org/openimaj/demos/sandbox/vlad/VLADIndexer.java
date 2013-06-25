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
package org.openimaj.demos.sandbox.vlad;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.MultidimensionalFloatFV;
import org.openimaj.image.feature.local.aggregate.VLAD;
import org.openimaj.image.feature.local.keypoints.FloatKeypoint;
import org.openimaj.io.IOUtils;
import org.openimaj.knn.pq.FloatProductQuantiser;
import org.openimaj.ml.pca.FeatureVectorPCA;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.util.pair.IntObjectPair;

public class VLADIndexer {
	public VLAD<float[]> vlad;
	public FeatureVectorPCA pca;
	public FloatProductQuantiser pq;

	public VLADIndexer(VLAD<float[]> vlad, FeatureVectorPCA pca, FloatProductQuantiser pq) {
		this.vlad = vlad;
		this.pca = pca;
		this.pq = pq;
	}

	public void save(File file) throws IOException {
		IOUtils.writeToFile(this, file);
	}

	public static VLADIndexer read(File file) throws IOException {
		return IOUtils.readFromFile(file);
	}

	public float[] extract(List<FloatKeypoint> normalisedKeypoints) {
		final MultidimensionalFloatFV keys = vlad.aggregate(normalisedKeypoints);

		if (keys == null)
			return null;

		final DoubleFV subspaceVector = pca.project(keys).normaliseFV(2);
		return ArrayUtils.convertToFloat(subspaceVector.values);
	}

	public List<IntObjectPair<float[]>> index(List<FloatKeypoint> normalisedKeypoints, int id,
			List<IntObjectPair<float[]>> index)
	{
		if (index == null)
			index = new ArrayList<IntObjectPair<float[]>>();

		index.add(new IntObjectPair<float[]>(id, extract(normalisedKeypoints)));

		return index;
	}

	public static VLADIndexer read(InputStream is) throws IOException {
		return IOUtils.read(new DataInputStream(is));
	}
}
