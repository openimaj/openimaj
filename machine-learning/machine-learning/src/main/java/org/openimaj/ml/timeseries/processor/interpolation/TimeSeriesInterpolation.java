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
package org.openimaj.ml.timeseries.processor.interpolation;

import org.openimaj.ml.timeseries.processor.TimeSeriesProcessor;
import org.openimaj.ml.timeseries.processor.interpolation.util.TimeSpanUtils;
import org.openimaj.ml.timeseries.series.DoubleTimeSeries;

/**
 * Interpolate values of a time series. Useful for filling in missing values and homogonising 
 * disperate sets of {@link TimeSeries} data
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public abstract class TimeSeriesInterpolation implements TimeSeriesProcessor<double[],Double,DoubleTimeSeries> {
	
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
	 * @return a new {@link DoubleTimeSeries} interpolated with times provided in the constructor
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
