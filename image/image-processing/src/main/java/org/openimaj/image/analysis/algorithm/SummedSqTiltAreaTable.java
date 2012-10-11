package org.openimaj.image.analysis.algorithm;

import org.openimaj.image.FImage;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * Implementation of an Integral Image or Summed Area Table. This Implementation
 * calculates both the sum, squared sum values, and (optionally) 45-degree
 * tilted sum values.
 * <p>
 * See http://en.wikipedia.org/wiki/Summed_area_table and
 * http://research.microsoft
 * .com/en-us/um/people/viola/Pubs/Detect/violaJones_IJCV.pdf
 * <p>
 * Basically, this provides an efficient way to find the sum of all pixels in a
 * rectangular area of an image.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class SummedSqTiltAreaTable extends SummedSqAreaTable {
	/**
	 * The tilted sum data
	 */
	public FImage tiltSum;

	/**
	 * Construct an empty SAT.
	 */
	public SummedSqTiltAreaTable() {
	}

	/**
	 * Construct a SAT for normal sum, squared sum and tilted sum from the
	 * provided image.
	 * 
	 * @param image
	 *            the image.
	 */
	public SummedSqTiltAreaTable(FImage image) {
		this(image, true);
	}

	/**
	 * Construct a SAT for normal sum, squared sum and (optionally) tilted sum
	 * from the provided image.
	 * 
	 * @param image
	 *            the image.
	 * @param computeTilted
	 *            if true compute the tilted features.
	 */
	public SummedSqTiltAreaTable(FImage image, boolean computeTilted) {
		computeTable(image, true);
	}

	private void computeTable(FImage image, boolean computeTilted) {
		if (computeTilted) {
			computeRotSqSumIntegralImages(image);
		} else {
			computeSqSumIntegralImages(image);
		}
	}

	protected void computeSqSumIntegralImages(FImage img) {
		final int width = img.width;
		final int height = img.height;

		sum = new FImage(width + 1, height + 1);
		sqSum = new FImage(width + 1, height + 1);

		final float[][] sumData = sum.pixels;
		final float[][] sqSumData = sqSum.pixels;

		for (int y = 1; y <= height; y++) {
			float rowSum = 0;
			float rowSumSQ = 0;

			final float[] row = img.pixels[y - 1];
			for (int x = 1; x <= width; x++) {
				final float pix = row[x - 1];

				rowSum += pix;
				rowSumSQ += pix * pix;

				sumData[y][x] = sumData[y - 1][x] + rowSum;
				sqSumData[y][x] = sqSumData[y - 1][x] + rowSumSQ;
			}
		}
	}

	protected final void computeRotSqSumIntegralImages(FImage image) {
		final int width = image.width;
		final int height = image.height;

		sum = new FImage(width + 1, height + 1);
		sqSum = new FImage(width + 1, height + 1);
		tiltSum = new FImage(width + 1, height + 1);

		// go through the image summing the data
		final float[] lastRow = new float[width];

		// first two rows are special
		// y == 1
		if (height > 0) {
			final float[] row = image.pixels[0];

			float rowSum = 0;
			float sqRowSum = 0;

			for (int x = 1; x <= width; x++) {
				final int gray = (int) (row[x - 1]);

				rowSum += gray;
				sqRowSum += gray * gray;

				sum.pixels[1][x] = rowSum;
				lastRow[x - 1] = tiltSum.pixels[1][x] = gray;
				sqSum.pixels[1][x] = sqRowSum;
			}
		}

		// y == 2
		if (height > 1) {
			final float[] row = image.pixels[1];

			int rowSum = 0;
			long sqRowSum = 0;

			for (int x = 1; x < width; x++) {
				final float gray = (row[x - 1]);

				rowSum += gray;
				sqRowSum += gray * gray;

				sum.pixels[2][x] = sum.pixels[1][x] + rowSum;
				sqSum.pixels[2][x] = sqSum.pixels[1][x] + sqRowSum;
				tiltSum.pixels[2][x] = tiltSum.pixels[1][x - 1] + lastRow[x - 1] + tiltSum.pixels[1][x + 1] + gray;
				lastRow[x - 1] = gray;
			}

			// last column is special
			if (width > 0) {
				final float gray = (row[width - 1]);

				rowSum += gray;
				sqRowSum += gray * gray;

				sum.pixels[2][width] = sum.pixels[1][width] + rowSum;
				sqSum.pixels[2][width] = sqSum.pixels[1][width] + sqRowSum;
				tiltSum.pixels[2][width] = tiltSum.pixels[1][width - 1] + lastRow[width - 1] + gray;
				lastRow[width - 1] = gray;
			}
		}

		for (int y = 3; y <= height; y++) {
			final float[] row = image.pixels[y - 1];

			int rowSum = 0;
			long sqRowSum = 0;

			if (width > 0) {
				final float gray = (row[0]);
				rowSum += gray;
				sqRowSum += gray * gray;

				sum.pixels[y][1] = sum.pixels[y - 1][1] + rowSum;
				sqSum.pixels[y][1] = sqSum.pixels[y - 1][1] + sqRowSum;
				tiltSum.pixels[y][1] = tiltSum.pixels[y - 1][2] + lastRow[0] + gray;
				lastRow[0] = gray;
			}

			for (int x = 2; x < width; x++) {
				final int gray = (int) (row[x - 1]);
				rowSum += gray;
				sqRowSum += gray * gray;

				sum.pixels[y][x] = sum.pixels[y - 1][x] + rowSum;
				sqSum.pixels[y][x] = sqSum.pixels[y - 1][x] + sqRowSum;
				tiltSum.pixels[y][x] = tiltSum.pixels[y - 1][x - 1] + lastRow[x - 1] + tiltSum.pixels[y - 1][x + 1]
						- tiltSum.pixels[y - 2][x] + gray;
				lastRow[x - 1] = gray;
			}

			if (width > 0) {
				final float gray = (row[width - 1]);
				rowSum += gray;
				sqRowSum += gray * gray;

				sum.pixels[y][width] = sum.pixels[y - 1][width] + rowSum;
				sqSum.pixels[y][width] = sqSum.pixels[y - 1][width] + sqRowSum;
				tiltSum.pixels[y][width] = tiltSum.pixels[y - 1][width - 1] + lastRow[width - 1] + gray;
				lastRow[width - 1] = gray;
			}
		}
	}

	/**
	 * Calculate the sum of pixels in the image used for constructing this SAT
	 * within the 45 degree tilted rectangle defined by (x1,y1) [top-left
	 * coordinate] and (x2,y2) [bottom- right coordinate].
	 * 
	 * @param x1
	 *            x1
	 * @param y1
	 *            y1
	 * @param x2
	 *            x2
	 * @param y2
	 *            y2
	 * @return sum of pixels in given rectangle
	 */
	public float calculateTiltedSumArea(int x1, int y1, int x2, int y2) {
		final float A = tiltSum.pixels[y1][x1];
		final float B = tiltSum.pixels[y1][x2];
		final float C = tiltSum.pixels[y2][x2];
		final float D = tiltSum.pixels[y2][x1];

		return A + C - B - D;
	}

	/**
	 * Calculate the sum pixels in the image used for constructing this SAT
	 * within the given 45-degree tilted rectangle
	 * 
	 * @param r
	 *            rectangle
	 * @return sum of pixels in given rectangle
	 */
	public float calculateTiltedSumArea(Rectangle r) {
		return calculateTiltedSumArea(Math.round(r.x), Math.round(r.y), Math.round(r.x + r.width),
				Math.round(r.y + r.height));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openimaj.image.analyser.ImageAnalyser#analyseImage(org.openimaj.image
	 * .Image)
	 */
	@Override
	public void analyseImage(FImage image) {
		computeTable(image, true);
	}
}
