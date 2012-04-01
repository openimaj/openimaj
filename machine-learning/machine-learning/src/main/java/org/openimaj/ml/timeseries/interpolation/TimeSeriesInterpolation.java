package org.openimaj.ml.timeseries.interpolation;

import org.openimaj.ml.timeseries.TimeSeries;
import org.openimaj.ml.timeseries.interpolation.util.TimeSpanUtils;
import org.openimaj.ml.timeseries.series.DoubleTimeSeries;

/**
 * Interpolate values of a time series. Useful for filling in missing values and homogonising 
 * disperate sets of {@link TimeSeries} data
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public abstract class TimeSeriesInterpolation implements TimeSeriesProcessor<double[],DoubleTimeSeries> {
	
	private long[] times;
	
	/**
	 * The processor's times are set to default, i.e. from min to max time in steps of 1 long
	 */
	public TimeSeriesInterpolation() {
		times = null;
	}
	
	
	/**
	 * @param begin 
	 * @param end 
	 * @param delta 
	 */
	public TimeSeriesInterpolation(long begin, long end, long delta) {
		this.times = TimeSpanUtils.getTime(begin,end, delta);
	}
	
	/**
	 * @param begin 
	 * @param steps 
	 * @param delta 
	 */
	public TimeSeriesInterpolation(long begin, int steps, long delta) {
		this.times = TimeSpanUtils.getTime(begin,steps, delta);
	}
	
	/**
	 * @param begin the start of the new time series
	 * @param end the end of the new time series
	 * @param steps the steps between (begin = 0, end = 10, 6 steps will give 0, 2, 4, 6, 8, 10
	 */
	public TimeSeriesInterpolation(long begin, long end, int steps) {
		this.times = TimeSpanUtils.getTime(begin,end, steps);
	}
	
	/**
	 * @param times the times used by the processor
	 */
	public TimeSeriesInterpolation(long[] times) {
		this.times = times;
	}
	
	/**
	 * Uses {@link #interpolate(DoubleTimeSeries, long[])} to return an interpolation of the construction
	 * {@link TimeSeries} between the times at the required interval
	 * @param series 
	 * @param begin time to start
	 * @param end time to end
	 * @param delta the delta between time steps
	 * @return {@link DoubleTimeSeries} instance interpolated from the construction {@link TimeSeries} instance
	 */
	public DoubleTimeSeries interpolate(DoubleTimeSeries series, long begin, long end, long delta){
		long[] times = TimeSpanUtils.getTime(begin,end,delta);
		return interpolate(series,times);
	}
	/**
	 * Uses {@link #interpolate(DoubleTimeSeries, long[])} to return an interpolation of the construction
	 * {@link TimeSeries} from begin, for a number of steps with a given delta between steps
	 * @param series 
	 * @param begin
	 * @param steps
	 * @param delta
	 * @return {@link DoubleTimeSeries} instance interpolated from the construction {@link TimeSeries} instance
	 */
	public DoubleTimeSeries interpolate(DoubleTimeSeries series, long begin, int steps, long delta){
		long[] times = TimeSpanUtils.getTime(begin,steps,delta);
		return interpolate(series,times);
	}
	/**
	 * Uses {@link #interpolate(DoubleTimeSeries,long[])} to return an interpolation of the construction
	 * {@link TimeSeries} from begin, until end with a delta which means that there are
	 * splits time instances
	 * @param series 
	 * @param begin 
	 * @param end
	 * @param splits
	 * @return {@link DoubleTimeSeries} instance interpolated from the construction {@link TimeSeries} instance
	 */
	public DoubleTimeSeries interpolate(DoubleTimeSeries series, long begin, long end, int splits){
		long[] times = TimeSpanUtils.getTime(begin,end,splits);
		return interpolate(series,times);
	}
	
	/**
	 * @param series
	 * @return
	 */
	public DoubleTimeSeries interpolate(DoubleTimeSeries series){
		return interpolate(series,this.times);
	}
	/**
	 * @param series 
	 * @param times might be null, therefore some "defualt" time step should be used
	 * @return {@link DoubleTimeSeries} instance interpolated from the construction {@link TimeSeries} instance
	 */
	public abstract DoubleTimeSeries interpolate(DoubleTimeSeries series, long[] times);
	
	@Override
	public void process(DoubleTimeSeries ts) {
		ts.internalAssign(this.interpolate(ts,times));
	}
}
