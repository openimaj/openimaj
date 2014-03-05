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
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				final float[] pixel = input.getPixelNative(x, y);
				final int centroid = assigner.assign(pixel);

				out.get(centroid).addPixel(x, y);
			}
		}

		return out;
	}
}
