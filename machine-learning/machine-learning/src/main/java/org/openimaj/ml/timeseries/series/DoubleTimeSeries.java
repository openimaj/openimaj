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
package org.openimaj.ml.timeseries.series;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Scanner;

import org.openimaj.io.ReadWriteableASCII;
import org.openimaj.ml.timeseries.TimeSeries;
import org.openimaj.ml.timeseries.TimeSeriesArithmaticOperator;
import org.openimaj.ml.timeseries.collection.TimeSeriesCollectionAssignable;
import org.openimaj.util.pair.IndependentPair;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class DoubleTimeSeries extends TimeSeries<double[],Double,DoubleTimeSeries>
	implements 
		TimeSeriesArithmaticOperator<Double, DoubleTimeSeries>,
		ReadWriteableASCII,
		TimeSeriesCollectionAssignable<Double, DoubleTimeSeries>,
		DoubleTimeSeriesProvider
{

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
		output.set(timeoutput, dataoutput);
		return output;
	}
	@Override
	public void set(long[] times, double[] data) {
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
		this.data = Arrays.copyOf(interpolate.data, interpolate.data.length);
		this.times = Arrays.copyOf(interpolate.times, interpolate.times.length);
		this.size = interpolate.size();
	}
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		String lf = "%d => %.5f\n";
		for (int i = 0; i < this.size(); i++) {
			long time = this.times[i];
			double data = this.data[i];
			sb.append(String.format(lf, time,data));
		}
		return sb.toString();
	}
	@Override
	public Iterator<IndependentPair<Long, Double>> iterator() {
		return new Iterator<IndependentPair<Long,Double>>() {
			int index = 0;
			@Override
			public boolean hasNext() {
				return index < DoubleTimeSeries.this.size;
			}

			@Override
			public IndependentPair<Long, Double> next() {
				IndependentPair<Long, Double> toret = IndependentPair.pair(DoubleTimeSeries.this.times[index],DoubleTimeSeries.this.data[index]);
				index++;
				return toret;
			}

			@Override
			public void remove() {
			}
		};
	}
	@Override
	public void readASCII(Scanner in) throws IOException {
		this.size = in.nextInt();
		this.data = new double[size];
		this.times = new long[size];
		for (int i = 0; i < size; i++) {
			times[i] = in.nextLong();
			data[i] = in.nextDouble();
		}
		
	}
	@Override
	public String asciiHeader() {
		return "";
	}
	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		out.print(this.size() + " ");
		for (IndependentPair<Long, Double> timedouble: this) {
			out.print(timedouble.firstObject() + " " + timedouble.secondObject() + " ");
		}
	}
	@Override
	public DoubleTimeSeries newInstance(Collection<Long> time,Collection<Double> data) {
		DoubleTimeSeries d = new DoubleTimeSeries();
		d.internalAssign(time, data);
		return d;
	}
	@Override
	public void internalAssign(Collection<Long> time, Collection<Double> data) {
		this.times = new long[time.size()];
		this.data = new double[time.size()];
		this.size = data.size();
		int i = 0;
		for (Long l : time)  this.times[i++] = l;
		i = 0;
		for (Double d : data)  this.data[i++] = d;
	}
	@Override
	public void internalAssign(long[] times, double[] data) {
		this.times = times;
		this.data = data;
		this.size = times.length;
		
	}
	@Override
	public Double zero() {
		return 0d;
	}
	@Override
	public Double sum() {
		double s = 0;
		for (int i = 0; i < this.data.length; i++) {
			s += this.data[i];
		}
		return s;
	}
	@Override
	public DoubleTimeSeries doubleTimeSeries() {
		return this;
	}	
}
