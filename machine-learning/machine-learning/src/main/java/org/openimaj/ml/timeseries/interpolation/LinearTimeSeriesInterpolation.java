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
		double[] values = new double[times.length];
		DoubleTimeSeries timeSeries = this.getSeries();
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
