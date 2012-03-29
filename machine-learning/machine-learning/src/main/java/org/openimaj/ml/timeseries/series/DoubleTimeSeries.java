package org.openimaj.ml.timeseries.series;

import java.util.Arrays;

import org.openimaj.ml.timeseries.TimeSeries;
import org.openimaj.ml.timeseries.TimeSeriesSetException;

/**
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class DoubleTimeSeries extends TimeSeries<double[]>{

	private static final int[] SINGLE_NOT_FOUND = new int[]{0,0};
	private long[] times;
	private double[] data;
	
	
	private int[] findStartEnd(long time, int nbefore, int nafter){
		int index = Arrays.binarySearch(times, time);
		int fixed = index < 0 ? -1 * (index + 1) : index;
		fixed = fixed == times.length ? times.length - 1 : fixed;
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
		return new int[]{start,end};
	}
	@Override
	public double[] get(long time, int nbefore, int nafter) {
		int[] startend = findStartEnd(time, nbefore, nafter);
		double[] output = new double[startend[1] - startend[0]];
		System.arraycopy(this.data, startend[0], output, 0, output.length);
		return output;
	}
	
	@Override
	public double[] get(long time, int nbefore, int nafter, double[] output) {
		int[] startend = findStartEnd(time, nbefore, nafter);
		System.arraycopy(this.data, startend[0], output, 0, startend[1]-startend[0]);
		return output;
	}

	@Override
	public double[] get(long time, long threshbefore, long threshafter) {
		int[] startend = findStartEnd(time, 0, 0);
		int start = startend[0];
		int end = start;
		// Find the index range
		while(start > 0 && times[start-1] >= time - threshbefore) start--;
		
		while(end < times.length && times[end] <= time + threshafter) end++;
		
		double[] output = new double[end - start];
		System.arraycopy(this.data, start, output, 0, output.length);
		return output;
	}

	@Override
	public void set(long[] times, double[] data) throws TimeSeriesSetException {
		this.times = times;
		this.data = data;
	}
	@Override
	public long[] getTimes() {
		return this.times;
	}
	@Override
	public double[] getData() {
		return this.data;
	}

}
