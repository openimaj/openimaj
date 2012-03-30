package org.openimaj.ml.timeseries.interpolation;

import org.openimaj.ml.timeseries.TimeSeries;

/**
 * A time series processor alters a type of {@link TimeSeries} in place.
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 * @param <DATA>
 * @param <TIMESERIES>
 */
public interface TimeSeriesArrayDataProcessor<DATA, TIMESERIES extends TimeSeries<DATA[], TIMESERIES>> extends TimeSeriesProcessor<DATA[], TIMESERIES>{
	/**
	 * @param series alter this time series in place
	 */
	@Override
	public void process(TIMESERIES series);
}
