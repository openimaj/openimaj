package org.openimaj.image.analysis.algorithm.histogram.binning;

import org.openimaj.image.analysis.algorithm.histogram.WindowedHistogramExtractor;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.statistics.distribution.Histogram;

/**
 * Interface that describes the creation of a histogram from a spatial region of
 * an image based on sub-histograms extracted using a
 * {@link WindowedHistogramExtractor}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public interface SpatialBinningStrategy {
	/**
	 * Extract a histogram describing image content in the given region using
	 * the given {@link WindowedHistogramExtractor} to extract (sub) histograms
	 * from which to build the output.
	 * <p>
	 * For efficiency, this method allows the output histogram to be specified
	 * as an input. This means that implementors of this interface can attempt
	 * to fill the output histogram rather than creating a new instance
	 * (although care should be taken to ensure that the porivded output
	 * histogram is the correct size and not <code>null</code>).
	 * <p>
	 * Users of {@link SpatialBinningStrategy}s should use the following style
	 * for maximum efficiency: <code><pre>
	 * Histogram h = null;
	 * ...
	 * for (Rectangle region : lots_of_regions)
	 * 	h = strategy.extract(binnedData, region, h);
	 * </pre></code>
	 * 
	 * @param binnedData
	 *            the {@link WindowedHistogramExtractor} to extract
	 *            sub-histograms from
	 * @param region
	 *            the region to extract from
	 * @param output
	 *            the output histogram to fill (can be null)
	 * @return the extracted histogram (preferably <code>output</code>)
	 */
	public Histogram extract(WindowedHistogramExtractor binnedData, Rectangle region, Histogram output);
}
