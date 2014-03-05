package org.openimaj.image.segmentation;

import org.openimaj.feature.FloatFVComparator;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;

/**
 * Simple image segmentation from grouping colours with k-means, and also
 * incorporating a spatial aspect based on pixel location.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class KMSpatialColourSegmenter extends KMColourSegmenter {

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
	public KMSpatialColourSegmenter(ColourSpace colourSpace, int K) {
		super(colourSpace, K);
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
	public KMSpatialColourSegmenter(ColourSpace colourSpace, int K, FloatFVComparator distance) {
		super(colourSpace, K, distance);
	}

	/**
	 * Construct using the given colour space, number of segments, and distance
	 * measure. The elements of each colour band are by the corresponding
	 * elements in the given scaling vector; the scaling vector should be two
	 * elements longer than the number of colour bands of the target colour
	 * space such that the last two elements correspond to the scalings for the
	 * normalised x and y positions of the pixels. Up to 100 K-Means iterations
	 * will be performed.
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
	public KMSpatialColourSegmenter(ColourSpace colourSpace, float[] scaling, int K, FloatFVComparator distance) {
		super(colourSpace, scaling, K, distance);
	}

	/**
	 * Construct using the given colour space, number of segments, and distance
	 * measure. The elements of each colour band are by the corresponding
	 * elements in the given scaling vector; the scaling vector should be two
	 * elements longer than the number of colour bands of the target colour
	 * space such that the last two elements correspond to the scalings for the
	 * normalised x and y positions of the pixels. The k-means algorithm will
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
	public KMSpatialColourSegmenter(ColourSpace colourSpace, float[] scaling, int K, FloatFVComparator distance,
			int maxIters)
	{
		super(colourSpace, scaling, K, distance, maxIters);
	}

	@Override
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
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				f[x + y * width][bands] = ((float) x / (float) width) * (scaling == null ? 1 : scaling[bands]);
				f[x + y * width][bands + 1] = ((float) y / (float) height) * (scaling == null ? 1 : scaling[bands + 1]);
			}
		}

		return f;
	}
}
