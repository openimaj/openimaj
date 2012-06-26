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

import org.openimaj.ml.timeseries.processor.interpolation.util.TimeSpanUtils;
import org.openimaj.ml.timeseries.series.DoubleTimeSeries;

/**
 * Perform a linear interpolation such that the value of data at time t1 between t0 and t2 = 
 * 
 * data[t1] = data[t0] * (t1 - t0)/(t2-t0) + data[t2] * (t2 - t1)/(t2-t0)
 * 
 * Note that this means if data is known at t1, then t0 = t1 and data[t1] == data[t0]
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class LinearInterpolationProcessor extends TimeSeriesInterpolation{
	
	
	
	/**
	 * @see TimeSeriesInterpolation#TimeSeriesInterpolation(long[])
	 * @param times
	 */
	public LinearInterpolationProcessor(long[] times) {
		super(times);
	}

	/**
	 * @see TimeSeriesInterpolation#TimeSeriesInterpolation()
	 */
	public LinearInterpolationProcessor() {
		super();
	}

	

	/**
	 * @param begin
	 * @param steps
	 * @param delta
	 */
	public LinearInterpolationProcessor(long begin, int steps, long delta) {
		super(begin, steps, delta);
	}

	/**
	 * @param begin
	 * @param end
	 * @param steps
	 */
	public LinearInterpolationProcessor(long begin, long end, int steps) {
		super(begin, end, steps);
	}

	/**
	 * @param begin
	 * @param end
	 * @param delta
	 */
	public LinearInterpolationProcessor(long begin, long end, long delta) {
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
