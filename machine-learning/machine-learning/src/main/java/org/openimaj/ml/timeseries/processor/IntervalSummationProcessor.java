/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.ml.timeseries.processor;

import java.util.LinkedList;

import org.openimaj.ml.timeseries.TimeSeries;
import org.openimaj.ml.timeseries.TimeSeriesArithmaticOperator;
import org.openimaj.ml.timeseries.collection.TimeSeriesCollectionAssignable;

/**
 * Given time step calculate each timestep such that 
 * value[timeStep(x)] = sum from x-1 to x as n [ timeStep(n) ]
 * 
 * The exact meaning of "sum" for any given timestep data must be defined. This processor works on any time series, 
 * a function must be implemented to explain how TimeSeries data is to be added.
 * 
 * This processor implicity assumes that the first time step is "the beggining of the time series"
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 * @param <ALLDATA>
 * @param <DATA>
 * @param <TS>
 */
public class IntervalSummationProcessor
	<
		ALLDATA,
		DATA,
		TS extends 
			TimeSeries<ALLDATA,DATA,TS> 
			& TimeSeriesArithmaticOperator<DATA,TS> 
			& TimeSeriesCollectionAssignable<DATA,TS>
	> 
	implements TimeSeriesProcessor<ALLDATA,DATA, TS>{
	
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
