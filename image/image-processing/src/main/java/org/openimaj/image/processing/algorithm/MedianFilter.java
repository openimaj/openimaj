package org.openimaj.image.processing.algorithm;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.openimaj.image.FImage;
import org.openimaj.image.processor.SinglebandImageProcessor;

/**
 * Median filter; replaces each pixel with the median of its neighbours.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class MedianFilter implements SinglebandImageProcessor<Float, FImage> {
	/**
	 * Offsets for using a 3x3 cross shaped mask to select pixels for computing
	 * median.
	 */
	public final static int[][] CROSS_3x3 = {
			{ 0, -1 },
			{ -1, 0 }, { 0, 0 }, { 1, 0 },
			{ 0, 1 }
	};

	/**
	 * Offsets for using a 3x3 blocked shaped mask to select pixels for
	 * computing median.
	 */
	public final static int[][] BLOCK_3x3 = {
			{ -1, -1 }, { 0, -1 }, { 1, -1 },
			{ -1, 0 }, { 0, 0 }, { 1, 0 },
			{ -1, 1 }, { 0, 1 }, { 1, 1 }
	};

	private int[][] support;
	private DescriptiveStatistics ds;

	/**
	 * Construct with the given support region for selecting pixels to take the
	 * median from. The support mask is a
	 * <code>[n][2]<code> array of <code>n</code> relative x, y offsets from the
	 * pixel currently being processed.
	 * 
	 * @param support
	 *            the support coordinates
	 */
	public MedianFilter(int[][] support) {
		this.support = support;
		this.ds = new DescriptiveStatistics(support.length);
	}

	@Override
	public void processImage(FImage image) {
		for (int y = 0; y < image.height; y++) {
			for (int x = 0; x < image.width; x++) {
				ds.clear();

				for (int i = 0; i < support.length; i++) {
					final int xx = x + support[i][0];
					final int yy = y + support[i][1];

					if (xx >= 0 && xx < image.width - 1 && yy >= 0 && yy < image.height - 1)
						ds.addValue(image.pixels[yy][xx]);
				}

				image.pixels[y][x] = (float) ds.getPercentile(50);
			}
		}
	}
}
