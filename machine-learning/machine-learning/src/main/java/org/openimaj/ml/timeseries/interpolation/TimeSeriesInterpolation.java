package org.openimaj.ml.timeseries.interpolation;

import org.openimaj.ml.timeseries.TimeSeries;
import org.openimaj.ml.timeseries.series.DoubleTimeSeries;
import org.openimaj.ml.timeseries.series.DoubleTimeSeriesProvider;

/**
 * Interpolate values of a time series. Useful for filling in missing values and homogonising 
 * disperate sets of {@link TimeSeries} data
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public abstract class TimeSeriesInterpolation {
	
	private DoubleTimeSeries series;
	
	/**
	 * @param series the time series to interpolate against
	 */
	public TimeSeriesInterpolation(DoubleTimeSeries series) {
		this.series = series;
	}
	
	/**
	 * @param provider the source of the time series
	 */
	public TimeSeriesInterpolation(DoubleTimeSeriesProvider provider) {
		this.series = provider.doubleTimeSeries();
	}
	
	/**
	 * @return the underlying series to be interpolated against
	 */
	public DoubleTimeSeries getSeries() {
		return series;
	}
	
	/**
	 * Uses {@link #interpolate(long[])} to return an interpolation of the construction
	 * {@link TimeSeries} between the times at the required interval
	 * @param begin time to start
	 * @param end time to end
	 * @param delta the delta between time steps
	 * @return {@link DoubleTimeSeries} instance interpolated from the construction {@link TimeSeries} instance
	 */
	public DoubleTimeSeries interpolate(long begin, long end, long delta){
		long[] times = new long[(int) ((end - begin)/delta)];
		long val = begin;
		for (int i = 0; i < times.length; i++) {
			times[i] = val;
			val += delta;
		}
		return interpolate(times);
	}
	/**
	 * Uses {@link #interpolate(long[])} to return an interpolation of the construction
	 * {@link TimeSeries} from begin, for a number of steps with a given delta between steps
	 * @param begin
	 * @param steps
	 * @param delta
	 * @return {@link DoubleTimeSeries} instance interpolated from the construction {@link TimeSeries} instance
	 */
	public DoubleTimeSeries interpolate(long begin, int steps, long delta){
		long[] times = new long[steps];
		long val = begin;
		for (int i = 0; i < times.length; i++) {
			times[i] = val;
			val += delta;
		}
		return interpolate(times);
	}
	/**
	 * Uses {@link #interpolate(long[])} to return an interpolation of the construction
	 * {@link TimeSeries} from begin, until end with a delta which means that there are
	 * splits time instances
	 * @param begin 
	 * @param end
	 * @param splits
	 * @return {@link DoubleTimeSeries} instance interpolated from the construction {@link TimeSeries} instance
	 */
	public DoubleTimeSeries interpolate(long begin, long end, int splits){
		long[] times = new long[splits];
		long delta = (end - begin) / splits;
		long val = begin;
		for (int i = 0; i < times.length; i++) {
			times[i] = val;
			val += delta;
		}
		return interpolate(times);
	}
	/**
	 * @param times
	 * @return {@link DoubleTimeSeries} instance interpolated from the construction {@link TimeSeries} instance
	 */
	public abstract DoubleTimeSeries interpolate(long[] times);
}
