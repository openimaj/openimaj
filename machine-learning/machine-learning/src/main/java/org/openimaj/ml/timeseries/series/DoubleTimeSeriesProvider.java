package org.openimaj.ml.timeseries.series;

/**
 * Allows a given time series to express itself as a {@link DoubleTimeSeries}
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public interface DoubleTimeSeriesProvider {
	/**
	 * @return provide an instance of a double time series for this time series
	 */
	public DoubleTimeSeries doubleTimeSeries();
}
