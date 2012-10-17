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
package org.openimaj.ml.timeseries.collection;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.openimaj.ml.timeseries.IncompatibleTimeSeriesException;
import org.openimaj.ml.timeseries.TimeSeries;
import org.openimaj.ml.timeseries.TimeSeriesSetException;

/**
 * A collection of time series which share exactly the same time steps.
 * Given that this is true, SynchronisedTimeSeriesProcessor instances can perform interesting analysis
 *  
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @param <ALLINPUT> The collection input type
 * @param <SINGLEINPUT> The type of a single time point data
 * @param <INTERNALSERIES> The type of the internal series held in this collection
 * @param <TIMESERIES> 
 *
 */
public abstract class SynchronisedTimeSeriesCollection<
	ALLINPUT, 
	SINGLEINPUT, 
	TIMESERIES extends SynchronisedTimeSeriesCollection<ALLINPUT, SINGLEINPUT,TIMESERIES,INTERNALSERIES>,
	INTERNALSERIES extends TimeSeries<ALLINPUT,SINGLEINPUT,INTERNALSERIES>
> extends TimeSeriesCollection<
	ALLINPUT, 
	SINGLEINPUT,
	TIMESERIES,
	INTERNALSERIES
>
{
	
	long[] time;

	/**
	 * initialise the underlying time series holder
	 */
	public SynchronisedTimeSeriesCollection(){
		super();
	}
	
	@Override
	public void addTimeSeries(String name, INTERNALSERIES series) throws IncompatibleTimeSeriesException{
		if(this.time == null){
			this.time = series.getTimes();
		}
		else if(!Arrays.equals(this.time, series.getTimes())){
			throw new IncompatibleTimeSeriesException();
		}
		
		super.addTimeSeries(name, series);
	}
	
	@Override
	public INTERNALSERIES series(String name){
		return this.timeSeriesHolder.get(name);
	}
	
	@Override
	public Collection<INTERNALSERIES> allseries(){
		return this.timeSeriesHolder.values();
	}

	@Override
	public long[] getTimes() {
		return this.time;
	}

	@Override
	public void set(long[] time, Map<String, ALLINPUT> data) throws TimeSeriesSetException {
		this.time = time;
		INTERNALSERIES intitalInstance = this.internalNewInstance();
		this.timeSeriesHolder = new HashMap<String,INTERNALSERIES>();
		for (Entry<String, ALLINPUT> l : data.entrySet()) {
			INTERNALSERIES intital = intitalInstance.newInstance();
			intital.set(time, l.getValue());
			this.timeSeriesHolder.put(l.getKey(), intital);
		}
	}

	@Override
	public Map<String, ALLINPUT> getData() {
		Map<String, ALLINPUT> ret = new HashMap<String, ALLINPUT>();
		for (Entry<String, INTERNALSERIES> held: this.timeSeriesHolder.entrySet()) {
			ret.put(held.getKey(), held.getValue().getData());
		}
		return ret ;
	}

	@Override
	public int size() {
		if(this.time!=null)return this.time.length;
		return 0;
	}

	@Override
	public void internalAssign(TIMESERIES interpolate) {
		this.time = interpolate.time;
		this.timeSeriesHolder = interpolate.timeSeriesHolder;
	}
	
	

	@Override
	public String toString() {
		return "Synchronised Time series";
	}
	
	/**
	 * In some way flatten the held time series such that the output is:
	 * 
	 * @return [ALLDATAs1t1,ALLDATAs2t1,...,ALLDATAs1,t2,...,ALLDATAsntm] etc.
	 */
	public abstract ALLINPUT flatten();
	
}
