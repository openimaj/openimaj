package org.openimaj.image.analysis.algorithm.histogram;

import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.statistics.distribution.Histogram;

/**
 * The primary purpose of this interface is to produce efficient access to
 * (rectangular) histograms of arbitrary windows of an image. A number of
 * methods are provided to extract such histograms.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public interface WindowedHistogramExtractor {

	/**
	 * Get the number of bins
	 * 
	 * @return the number of bins
	 */
	public abstract int getNumBins();

	/**
	 * Compute the histogram for the given window.
	 * 
	 * @param roi
	 *            the window
	 * @return the histogram in the window of the last analysed image
	 */
	public abstract Histogram computeHistogram(Rectangle roi);

	/**
	 * Compute the histogram for the given window.
	 * 
	 * @param x
	 *            The x-coordinate of the top-left of the window
	 * @param y
	 *            The y-coordinate of the top-left of the window
	 * @param w
	 *            The width of the window
	 * @param h
	 *            The height of the window
	 * @return the histogram in the window of the last analysed image
	 */
	public abstract Histogram computeHistogram(int x, int y, int w, int h);

	/**
	 * Compute the histogram for the given window, storing the output in the
	 * given {@link Histogram} object, which must have the same length as given
	 * by {@link #getNumBins()}.
	 * 
	 * @param roi
	 *            the window
	 * @param histogram
	 *            the histogram to write to
	 */
	public abstract void computeHistogram(Rectangle roi, Histogram histogram);

	/**
	 * Compute the histogram for the given window, storing the output in the
	 * given {@link Histogram} object, which must have the same length as given
	 * by {@link #getNumBins()}.
	 * 
	 * @param x
	 *            The x-coordinate of the top-left of the window
	 * @param y
	 *            The y-coordinate of the top-left of the window
	 * @param w
	 *            The width of the window
	 * @param h
	 *            The height of the window
	 * @param histogram
	 *            the histogram to write to
	 */
	public abstract void computeHistogram(int x, int y, int w, int h, Histogram histogram);
}
