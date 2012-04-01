package org.openimaj.ml.timeseries.interpolation;

import java.util.LinkedList;

import org.openimaj.ml.timeseries.TimeSeries;

/**
 * Given time step calculate each timestep such that 
 * value[timeStep(x)] = sum from x-1 to x as n [ timeStep(n) ]
 * 
 * The exact meaning of "sum" for any given timestep data must be defined. This processor works on any time series, 
 * a function must be implemented to explain how TimeSeries data is to be added.
 * 
 * This processor implicity assumes that the first time step is "the beggining of the time series"
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 * @param <DATA>
 * @param <TS>
 */
public class IntervalSummationProcessor
	<
		DATA, 
		TS extends 
			TimeSeries<DATA[],TS> 
			& TimeSeriesArithmaticOperator<DATA,TS> 
			& TimeSeriesCollectionAssignable<DATA,TS>
	> 
	implements TimeSeriesArrayDataProcessor<DATA, TS>{
	
	private long[] times;

	/**
	 * A processor which maps across given time steps
	 * @param times
	 */
	public IntervalSummationProcessor(long[] times) {
		this.times = times;
	}
	
	@Override
	public void process(TS series) {
		LinkedList<Long> times = new LinkedList<Long>();
		LinkedList<DATA> data  = new LinkedList<DATA>();
		times.addLast(this.times[0]);		
		long firstTime = series.getTimes()[0];
		
		TS interval = series.get(firstTime,this.times[0]);
		
		long previousTime = -1;
		if(interval.size() > 0){
			long[] intervalTimes = interval.getTimes();
			previousTime = intervalTimes[intervalTimes.length - 1] + 1;
		}
		else{
			previousTime = this.times[0] + 1;
		}
		
		data.addLast(interval.sum());
		for (int i = 1; i < this.times.length; i++) {
			long currentTime = this.times[i];
			interval = series.get(previousTime,currentTime);
			if(interval.size() > 0){
				long[] intervalTimes = interval.getTimes();
				previousTime = intervalTimes[intervalTimes.length - 1] + 1;
			}
			else{
				previousTime = currentTime + 1;
			}
			times.add(currentTime);
			data.add(interval.sum());
		}
		series.internalAssign(times,data);
	}
}
