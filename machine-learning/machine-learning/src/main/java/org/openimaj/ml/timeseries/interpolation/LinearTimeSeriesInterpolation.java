package org.openimaj.ml.timeseries.interpolation;

import org.openimaj.ml.timeseries.interpolation.util.TimeSpanUtils;
import org.openimaj.ml.timeseries.series.DoubleTimeSeries;

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
	 * @see TimeSeriesInterpolation#TimeSeriesInterpolation(long[])
	 * @param times
	 */
	public LinearTimeSeriesInterpolation(long[] times) {
		super(times);
	}

	/**
	 * @see TimeSeriesInterpolation#TimeSeriesInterpolation()
	 */
	public LinearTimeSeriesInterpolation() {
		super();
	}

	

	/**
	 * @param begin
	 * @param steps
	 * @param delta
	 */
	public LinearTimeSeriesInterpolation(long begin, int steps, long delta) {
		super(begin, steps, delta);
	}

	/**
	 * @param begin
	 * @param end
	 * @param steps
	 */
	public LinearTimeSeriesInterpolation(long begin, long end, int steps) {
		super(begin, end, steps);
	}

	/**
	 * @param begin
	 * @param end
	 * @param delta
	 */
	public LinearTimeSeriesInterpolation(long begin, long end, long delta) {
		super(begin, end, delta);
	}

	@Override
	public DoubleTimeSeries interpolate(DoubleTimeSeries timeSeries, long[] times) {
		if(times == null){
			times = TimeSpanUtils.getTime(timeSeries.getTimes()[0],timeSeries.getTimes()[timeSeries.size()-1],1l);
		}
		double[] values = new double[times.length];
		DoubleTimeSeries dataholder = new DoubleTimeSeries(3);
		double[] holderdata = dataholder.getData();
		long[] holdertimes = dataholder.getTimes();
		int i = 0;
		for (long t : times) {
			timeSeries.get(t, 1, 1,dataholder);
			if(dataholder.size() == 3){ // In the middle
				values[i++] = holderdata[1];
			}
			else if(dataholder.size() == 2){
				// Either left or right extreme
				if(holdertimes[0] == t){
					values[i++] = holderdata[0];
				}
				else if(holdertimes[1] == t){
					values[i++] = holderdata[1];
				}
				else{
					// This is the only point we should interpolate
					double sum = holdertimes[1] - holdertimes[0];
					double weightLeft = sum - (t - holdertimes[0]);
					double weightRight = sum - (holdertimes[1] - t);
					values[i++] = ((holderdata[0] * weightLeft) + (holderdata[1] * weightRight))/sum;
				}
			}
			else{
				values[i++] = holderdata[0];
			}
		}
		return new DoubleTimeSeries(times,values);
	}

}
