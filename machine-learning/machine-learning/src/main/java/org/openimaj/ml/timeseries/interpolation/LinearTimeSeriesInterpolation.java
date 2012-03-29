package org.openimaj.ml.timeseries.interpolation;

import org.openimaj.ml.timeseries.series.DoubleTimeSeries;
import org.openimaj.ml.timeseries.series.DoubleTimeSeriesProvider;

/**
 * Perform a linear interpolation such that the value of data at time t1 between t0 and t2 = 
 * 
 * data[t1] = data[t0] * (t1 - t0)/(t2-t0) + data[t2] * (t2 - t1)/(t2-t0)
 * 
 * Note that this means if data is known at t1, then t0 = t1 and data[t1] == data[t0]
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class LinearTimeSeriesInterpolation extends TimeSeriesInterpolation{

	/**
	 * @param series
	 */
	public LinearTimeSeriesInterpolation(DoubleTimeSeries series) {
		super(series);
	}
	
	/**
	 * @param provider
	 */
	public LinearTimeSeriesInterpolation(DoubleTimeSeriesProvider provider) {
		super(provider);
	}

	@Override
	public DoubleTimeSeries interpolate(long[] times) {
		return null;
	}

}
