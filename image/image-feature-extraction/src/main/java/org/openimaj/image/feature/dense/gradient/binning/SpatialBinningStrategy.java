package org.openimaj.image.feature.dense.gradient.binning;

import org.openimaj.image.FImage;
import org.openimaj.image.analysis.algorithm.BinnedImageHistogramAnalyser;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.statistics.distribution.Histogram;

/**
 * Interface that describes how a histogram should be created from a spatial
 * region of pre-binned values in a {@link BinnedImageHistogramAnalyser}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public interface SpatialBinningStrategy {
	public Histogram extract(BinnedImageHistogramAnalyser binnedData, FImage magnitudes, Rectangle region);
}
