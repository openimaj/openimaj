package org.openimaj.image.processing.convolution;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.image.FImage;
import org.openimaj.image.processor.SinglebandImageProcessor;

/**
 * Fast approximate Gaussian smoothing using repeated fast box filtering.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
@Reference(
		type = ReferenceType.Inproceedings,
		author = { "Kovesi, P." },
		title = "Fast Almost-Gaussian Filtering",
		year = "2010",
		booktitle = "Digital Image Computing: Techniques and Applications (DICTA), 2010 International Conference on",
		pages = { "121", "125" },
		month = "Dec",
		customData = {
				"keywords", "Gaussian processes;approximation theory;band-pass filters;image processing;Gaussian bandpass filters;fast almost-Gaussian filtering;image averaging;integral images;log-Gabor filters;separable moving average filters;summed area tables;symmetric transfer function;Approximation methods;Bandwidth;Computer vision;Frequency domain analysis;Laplace equations;Pixel;Transfer functions;Difference of Gaussian filtering;Gaussian smoothing",
				"doi", "10.1109/DICTA.2010.30"
		})
public class FFastGaussianConvolve implements SinglebandImageProcessor<Float, FImage> {
	private final int n;
	private final int m;
	private SinglebandImageProcessor<Float, FImage> wlBox;
	private SinglebandImageProcessor<Float, FImage> wuBox;

	/**
	 * Construct an {@link FFastGaussianConvolve} to approximate blurring with a
	 * Gaussian of standard deviation sigma.
	 * 
	 * @param sigma
	 *            Standard deviation of the approximated Gaussian
	 * @param n
	 *            Number of filtering iterations to perform (usually between 3
	 *            and 6)
	 */
	public FFastGaussianConvolve(float sigma, int n) {
		if (sigma < 1.8) {
			// std.devs of less than 1.8 are not well approximated.
			this.m = 1;
			this.n = 1;
			this.wlBox = new FGaussianConvolve(sigma);
		} else {
			final float ss = sigma * sigma;
			final double wIdeal = Math.sqrt((12.0 * ss / n) + 1.0);
			final int wl = (((int) wIdeal) % 2 == 0) ? (int) wIdeal - 1 : (int) wIdeal;
			final int wu = wl + 2;

			this.n = n;
			this.m = Math.round((12 * ss - n * wl * wl - 4 * n * wl - 3 * n) / (-4 * wl - 4));

			this.wlBox = new AverageBoxFilter(wl);
			this.wuBox = new AverageBoxFilter(wu);
		}
	}

	@Override
	public void processImage(FImage image) {
		for (int i = 0; i < m; i++)
			wlBox.processImage(image);
		for (int i = 0; i < n - m; i++)
			wuBox.processImage(image);
	}
}
