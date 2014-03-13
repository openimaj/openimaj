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
package org.openimaj.image.segmentation;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.feature.FloatFVComparator;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.pixel.PixelSet;
import org.openimaj.knn.FloatNearestNeighbours;
import org.openimaj.knn.FloatNearestNeighboursExact;
import org.openimaj.ml.clustering.FloatCentroidsResult;
import org.openimaj.ml.clustering.assignment.HardAssigner;
import org.openimaj.ml.clustering.kmeans.FloatKMeans;
import org.openimaj.ml.clustering.kmeans.KMeansConfiguration;

/**
 * Simple image segmentation from grouping colours with k-means.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class KMColourSegmenter implements Segmenter<MBFImage> {
	private static final int DEFAULT_MAX_ITERS = 100;
	protected ColourSpace colourSpace;
	protected float[] scaling;
	protected FloatKMeans kmeans;

	/**
	 * Construct using the given colour space and number of segments. Euclidean
	 * distance is used, and the elements of each colour band are unscaled. Up
	 * to 100 K-Means iterations will be performed.
	 * 
	 * @param colourSpace
	 *            the colour space
	 * @param K
	 *            the number of segments
	 */
	public KMColourSegmenter(ColourSpace colourSpace, int K) {
		this(colourSpace, null, K, null, DEFAULT_MAX_ITERS);
	}

	/**
	 * Construct using the given colour space, number of segments, and distance
	 * measure. The elements of each colour band are unscaled. Up to 100 K-Means
	 * iterations will be performed.
	 * 
	 * @param colourSpace
	 *            the colour space
	 * @param K
	 *            the number of segments
	 * @param distance
	 *            the distance measure
	 */
	public KMColourSegmenter(ColourSpace colourSpace, int K, FloatFVComparator distance) {
		this(colourSpace, null, K, distance, DEFAULT_MAX_ITERS);
	}

	/**
	 * Construct using the given colour space, number of segments, and distance
	 * measure. The elements of each colour band are by the corresponding
	 * elements in the given scaling vector. Up to 100 K-Means iterations will
	 * be performed.
	 * 
	 * @param colourSpace
	 *            the colour space
	 * @param scaling
	 *            the scaling vector
	 * @param K
	 *            the number of segments
	 * @param distance
	 *            the distance measure
	 */
	public KMColourSegmenter(ColourSpace colourSpace, float[] scaling, int K, FloatFVComparator distance) {
		this(colourSpace, scaling, K, distance, DEFAULT_MAX_ITERS);
	}

	/**
	 * Construct using the given colour space, number of segments, and distance
	 * measure. The elements of each colour band are by the corresponding
	 * elements in the given scaling vector, and the k-means algorithm will
	 * iterate at most <code>maxIters</code> times.
	 * 
	 * @param colourSpace
	 *            the colour space
	 * @param scaling
	 *            the scaling vector
	 * @param K
	 *            the number of segments
	 * @param distance
	 *            the distance measure
	 * @param maxIters
	 *            the maximum number of iterations to perform
	 */
	public KMColourSegmenter(ColourSpace colourSpace, float[] scaling, int K, FloatFVComparator distance, int maxIters) {
		if (scaling != null && scaling.length < colourSpace.getNumBands())
			throw new IllegalArgumentException(
					"Scaling vector must have the same length as the number of dimensions of the target colourspace (or more)");

		this.colourSpace = colourSpace;
		this.scaling = scaling;

		final KMeansConfiguration<FloatNearestNeighbours, float[]> conf =
				new KMeansConfiguration<FloatNearestNeighbours, float[]>(
						K,
						new FloatNearestNeighboursExact.Factory(distance),
						maxIters);

		this.kmeans = new FloatKMeans(conf);
	}

	protected float[][] imageToVector(MBFImage image) {
		final int height = image.getHeight();
		final int width = image.getWidth();
		final int bands = image.numBands();

		final float[][] f = new float[height * width][bands];
		for (int b = 0; b < bands; b++) {
			final float[][] band = image.getBand(b).pixels;
			final float w = scaling == null ? 1 : scaling[b];

			for (int y = 0; y < height; y++)
				for (int x = 0; x < width; x++)
					f[x + y * width][b] = band[y][x] * w;
		}

		return f;
	}

	@Override
	public List<? extends PixelSet> segment(final MBFImage image) {
		final MBFImage input = ColourSpace.convert(image, colourSpace);
		final float[][] imageData = imageToVector(input);

		final FloatCentroidsResult result = kmeans.cluster(imageData);

		final List<PixelSet> out = new ArrayList<PixelSet>(kmeans.getConfiguration().getK());
		for (int i = 0; i < kmeans.getConfiguration().getK(); i++)
			out.add(new PixelSet());

		final HardAssigner<float[], ?, ?> assigner = result.defaultHardAssigner();
		final int height = image.getHeight();
		final int width = image.getWidth();
		for (int y = 0, i = 0; y < height; y++) {
			for (int x = 0; x < width; x++, i++) {
				final float[] pixel = imageData[i];
				final int centroid = assigner.assign(pixel);

				out.get(centroid).addPixel(x, y);
			}
		}

		return out;
	}
}
