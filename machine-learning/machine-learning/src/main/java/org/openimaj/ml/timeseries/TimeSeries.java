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
package org.openimaj.ml.timeseries;

import org.openimaj.ml.timeseries.converter.TimeSeriesConverter;
import org.openimaj.ml.timeseries.processor.TimeSeriesProcessor;
import org.openimaj.util.pair.IndependentPair;

/**
 * A time series defines data at discrete points in time. The time series has
 * the ability to return data at a specific point in time, return neighbours
 * within some window, closest neighbours or n neighbours before and after a
 * time.
 * <p>
 * These values can be used by a {@link TimeSeriesInterpolation} to get specific
 * moments in time
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 * @param <DATA>
 *            the type of the data at each point in time
 * @param <SINGLE_TYPE>
 *            the type of the an element at a single point in time
 * @param <RETURNTYPE>
 *            the time series returned by the get
 * 
 */
public abstract class TimeSeries<DATA, SINGLE_TYPE, RETURNTYPE extends TimeSeries<DATA, SINGLE_TYPE, RETURNTYPE>>
		implements
			Iterable<IndependentPair<Long, SINGLE_TYPE>>
{

	/**
	 * Same as calling {@link #get(long, int, int)} with spans as 0
	 * 
	 * @param time
	 * @return the requested data or null.
	 */
	public RETURNTYPE get(long time) {
		return get(time, 0, 0);
	}

	/**
	 * returns the DATA at a specific point in time and those before and after
	 * to the number requested. This method may not return data for the specific
	 * requested time if it does not exists. If the time series is completely
	 * empty this function may return at an empty array however if at least 1
	 * data point exists and either nbefore and nafter are bigger than 1 then at
	 * least 1 datapoint will be returned.
	 * <p>
	 * Data should be returned in order
	 * 
	 * @param time
	 * @param nbefore
	 * @param nafter
	 * @return all data found with these parameters
	 */
	public abstract RETURNTYPE get(long time, int nbefore, int nafter);

	/**
	 * Same as {@link #get(long, int, int)} but instead of createing the output
	 * DATA instance, an existing data instance is handed which is filled. For
	 * convenience this output is also returned
	 * <p>
	 * Data should be returned in order
	 * 
	 * @param time
	 * @param nbefore
	 * @param nafter
	 * @param output
	 * @return all data found with these parameters
	 */
	public abstract RETURNTYPE get(long time, int nbefore, int nafter, RETURNTYPE output);

	/**
	 * returns the RETURNTYPE at a specific point in time and those before and
	 * after within the specified thresholds. This method may not return data
	 * for the specific requested time if it does not exists. Similarly this
	 * method may return an empty array if no time data is available within the
	 * window specified.
	 * <p>
	 * Data should be returned in order
	 * 
	 * @param time
	 * @param threshbefore
	 * @param threshafter
	 * @return all data found with these parameters
	 */
	public abstract RETURNTYPE get(long time, long threshbefore, long threshafter);

	/**
	 * returns the RETURNTYPE between the specified time periods. This method
	 * may not return data for the specific requested time if it does not
	 * exists. Similarly this method may return an empty array if no time data
	 * is available within the window specified.
	 * <p>
	 * Data should be returned in order
	 * 
	 * @param start
	 * @param end
	 * @return all data found with these parameters
	 */
	public abstract RETURNTYPE get(long start, long end);

	/**
	 * Set the data associated with each time. This function explicitly assumes
	 * that time.length == data.length and there exists a single data instance
	 * per time instance
	 * 
	 * @param time
	 *            instances of time
	 * @param data
	 *            instances of data
	 * @throws TimeSeriesSetException
	 */
	public abstract void set(long[] time, DATA data) throws TimeSeriesSetException;

	/**
	 * @return all times
	 */
	public abstract long[] getTimes();

	/**
	 * @return all data
	 */
	public abstract DATA getData();

	/**
	 * @return an empty new instance of this timeseries type
	 */
	public abstract RETURNTYPE newInstance();

	/**
	 * @return the number of valid time steps in this timeseries
	 */
	public abstract int size();

	/**
	 * @param interpolate
	 *            assign this timeseries to the internal one, efforts should be
	 *            made to copy the data, not simply assign it
	 */
	public abstract void internalAssign(RETURNTYPE interpolate);

	/**
	 * @param times
	 * @param data
	 */
	public void internalAssign(long[] times, DATA data) {
		try {
			this.set(times, data);
		} catch (final TimeSeriesSetException e) {
		}
	}

	/**
	 * @return clone this time series
	 */
	@SuppressWarnings("unchecked")
	public RETURNTYPE copy() {
		final RETURNTYPE t = newInstance();
		t.internalAssign((RETURNTYPE) this);
		return t;
	}

	/**
	 * process using the provided processor, return
	 * 
	 * @param tsp
	 * @return a new instance processed
	 */
	public RETURNTYPE process(TimeSeriesProcessor<DATA, SINGLE_TYPE, RETURNTYPE> tsp) {
		final RETURNTYPE copy = copy();
		tsp.process(copy);
		return copy;
	}

	@SuppressWarnings("unchecked")
	private RETURNTYPE self() {
		return (RETURNTYPE) this;
	}

	/**
	 * Process using the provided processor
	 * 
	 * @param tsp
	 * @return this object processed inplace
	 */
	public RETURNTYPE processInplace(TimeSeriesProcessor<DATA, SINGLE_TYPE, RETURNTYPE> tsp) {
		tsp.process(self());
		return self();
	}

	/**
	 * Convert a {@link TimeSeries}
	 * 
	 * @param <OUTDATA>
	 * @param <OUTSING>
	 * @param <OUTRET>
	 * @param converter
	 *            the converter
	 * @return the converted timeseries
	 */
	public <OUTDATA, OUTSING, OUTRET extends TimeSeries<OUTDATA, OUTSING, OUTRET>> OUTRET convert(
			TimeSeriesConverter<DATA, SINGLE_TYPE, RETURNTYPE, OUTDATA, OUTSING, OUTRET> converter)
	{
		return converter.convert(self());
	}

	/**
	 * Convert a {@link TimeSeries}
	 * 
	 * @param <OUTDATA>
	 * @param <OUTSING>
	 * @param <OUTRET>
	 * @param converter
	 *            the converter
	 * @param tsp
	 *            the processor
	 * @return the converted timeseries
	 */
	public <OUTDATA, OUTSING, OUTRET extends TimeSeries<OUTDATA, OUTSING, OUTRET>> OUTRET convert(
			TimeSeriesConverter<DATA, SINGLE_TYPE, RETURNTYPE, OUTDATA, OUTSING, OUTRET> converter,
			TimeSeriesProcessor<OUTDATA, OUTSING, OUTRET> tsp)
	{
		return converter.convert(self(), tsp);
	}

	@Override
	public abstract String toString();
}
