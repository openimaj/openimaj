package org.openimaj.image.analysis.algorithm.histogram.binning;

import org.openimaj.image.analysis.algorithm.histogram.WindowedHistogramExtractor;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.statistics.distribution.Histogram;

/**
 * Interface that describes how a histogram should be created from a spatial
 * region of pre-binned values in a {@link WindowedHistogramExtractor}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public interface SpatialBinningStrategy {
	public Histogram extract(WindowedHistogramExtractor binnedData, Rectangle region, Histogram output);
}
