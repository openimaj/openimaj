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
package org.openimaj.ml.timeseries.processor.interpolation.util;

/**
 * Some utility functions used by various {@link TimeSeries} classes to get arrays of spans of time
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TimeSpanUtils {
	/**
	 * Get
	 * @param begin
	 * @param end
	 * @param delta
	 * @return longs evenly spaced between from begin and less than end with spacings of delta
	 */
	public static long[] getTime(long begin, long end, long delta) {
		long[] times = new long[(int) ((end - begin)/delta) + 1];
		long val = begin;
		for (int i = 0; i < times.length; i++) {
			times[i] = val;
			val += delta;
		}
		return times;
	}
	
	/**
	 * @param begin
	 * @param end
	 * @param splits
	 * @return longs starting from begin and less than end such that "splits" times are returned and delta between the times is (end-begin)/(splits-1)
	 */
	public static long[] getTime(long begin, long end, int splits) {
		long[] times = new long[splits];
		long delta = (end - begin) / (splits-1);
		long val = begin;
		for (int i = 0; i < times.length; i++) {
			times[i] = val;
			val += delta;
		}
		return times;
	}
	
	/**
	 * @param begin
	 * @param steps
	 * @param delta
	 * @return "steps" longs starting from begin spaced by delta 
	 */
	public static long[] getTime(long begin, int steps, long delta){
		long[] times = new long[steps];
		long val = begin;
		for (int i = 0; i < times.length; i++) {
			times[i] = val;
			val += delta;
		}
		return times;
	}
}
