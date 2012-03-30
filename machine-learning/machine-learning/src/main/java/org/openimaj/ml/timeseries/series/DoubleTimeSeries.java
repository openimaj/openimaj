package org.openimaj.ml.timeseries.series;

import java.util.Arrays;

import org.openimaj.ml.timeseries.TimeSeries;
import org.openimaj.ml.timeseries.TimeSeriesSetException;
import org.openimaj.ml.timeseries.interpolation.TimeSeriesArithmaticOperator;

/**
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class DoubleTimeSeries extends TimeSeries<double[],DoubleTimeSeries>{

	private static final double[] ZERO_ARRAY = new double[]{0};
	private long[] times;
	private double[] data;
	int size = 0;
	
	/**
	 * Convenience constructor, makes a time series with empty data of a given size
	 * @param i
	 */
	public DoubleTimeSeries(int i) {
		this.times = new long[i];
		this.data = new double[i];
		size = i;
	}
	/**
	 * Sets the times and data arrays backing this class 0 length
	 */
	public DoubleTimeSeries() {
		this.times = new long[0];
		this.data = new double[0];
		size = 0;
	}
	/**
	 * 
	 * @param times
	 * @param data
	 */
	public DoubleTimeSeries(long[] times, double[] data) {
		this.times = times;
		this.data = data;
		size = this.data.length;
	}
	private int[] findStartEnd(long time, int nbefore, int nafter){
		int index = Arrays.binarySearch(times, time);
		int fixed = index < 0 ? -1 * (index + 1) : index;
		int start = 0;
		int end = times.length - 1;
		
		start = fixed- nbefore;
		// couldn't find it
		if(index < 0){
			end = fixed + nafter;
		}
		// could
		else{
			end = fixed + nafter + 1;
		}
		if(start < 0) start= 0;
		if(end > times.length) end = times.length;
		return new int[]{start,end};
	}
	@Override
	public DoubleTimeSeries get(long time, int nbefore, int nafter) {
		if(nbefore < 0 || nafter < 0)
		{
			return new DoubleTimeSeries();
		}
		int[] startend = findStartEnd(time, nbefore, nafter);
		double[] dataoutput = new double[startend[1] - startend[0]];
		System.arraycopy(this.data, startend[0], dataoutput, 0, dataoutput.length);
		long[] timeoutput = new long[startend[1] - startend[0]];
		System.arraycopy(this.times, startend[0], timeoutput, 0, timeoutput.length);
		DoubleTimeSeries output = newInstance(timeoutput,dataoutput);
		return output;
	}
	
	@Override
	public DoubleTimeSeries get(long time, int nbefore, int nafter, DoubleTimeSeries output) {
		int[] startend = findStartEnd(time, nbefore, nafter);
		System.arraycopy(this.data, startend[0], output.data, 0, startend[1]-startend[0]);
		System.arraycopy(this.times, startend[0], output.times, 0, startend[1]-startend[0]);
		output.size = startend[1]-startend[0];
		return output;
	}

	@Override
	public DoubleTimeSeries get(long time, long threshbefore, long threshafter) {
		if(threshafter < 0 || threshbefore < 0){
			return new DoubleTimeSeries();
		}
		int[] startend = findStartEnd(time, 0, 0);
		int start = startend[0];
		int end = start;
		// Find the index range
		while(start > 0 && times[start-1] >= time - threshbefore) start--;
		while(end < times.length && times[end] <= time + threshafter) end++;
		
		double[] dataoutput = new double[end - start];
		System.arraycopy(this.data, start, dataoutput, 0, dataoutput.length);
		long[] timeoutput = new long[end - start];
		System.arraycopy(this.times, start, timeoutput, 0, timeoutput.length);
		DoubleTimeSeries output = newInstance(timeoutput,dataoutput);
		return output;
	}
	
	@Override
	public DoubleTimeSeries get(long start, long end) {
		return get(start,0,end-start);
	}	

	private DoubleTimeSeries newInstance(long[] timeoutput, double[] dataoutput) {
		DoubleTimeSeries output = newInstance();
		try {output.set(timeoutput, dataoutput);} catch (TimeSeriesSetException e) {}
		return output;
	}
	@Override
	public void set(long[] times, double[] data) throws TimeSeriesSetException {
		this.times = times;
		this.data = data;
		this.size = data.length;
	}
	@Override
	public long[] getTimes() {
		return this.times;
	}
	@Override
	public double[] getData() {
		return this.data;
	}
	@Override
	public DoubleTimeSeries newInstance() {
		return new DoubleTimeSeries();
	}
	
	@Override
	public int size() {
		return size;
	}
	@Override
	public void internalAssign(DoubleTimeSeries interpolate) {
		this.data = interpolate.data;
		this.times = interpolate.times;
	}	
}
