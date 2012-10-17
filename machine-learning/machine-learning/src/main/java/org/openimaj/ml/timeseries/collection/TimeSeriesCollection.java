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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openimaj.ml.timeseries.IncompatibleTimeSeriesException;
import org.openimaj.ml.timeseries.TimeSeries;
import org.openimaj.ml.timeseries.TimeSeriesSetException;
import org.openimaj.ml.timeseries.converter.TimeSeriesConverter;
import org.openimaj.ml.timeseries.processor.TimeSeriesProcessor;
import org.openimaj.util.pair.IndependentPair;

/**
 * A collection of time series which share exactly the same time steps.
 * Given that this is true, SynchronisedTimeSeriesProcessor instances can perform interesting analysis
 *  
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @param <ALLINPUT> The collection input type 
 * @param <SINGLEINPUT> The type of a single data type 
 * @param <TIMESERIES> the type of time series returned
 * @param <INTERNALSERIES> the type of time series held
 *
 */
public abstract class TimeSeriesCollection<
	ALLINPUT, 
	SINGLEINPUT, 
	TIMESERIES extends TimeSeriesCollection<ALLINPUT, SINGLEINPUT,TIMESERIES,INTERNALSERIES>,
	INTERNALSERIES extends TimeSeries<ALLINPUT,SINGLEINPUT,INTERNALSERIES>
> extends TimeSeries<
	Map<String,ALLINPUT>, 
	Map<String,SINGLEINPUT>,
	TIMESERIES
> {
	
	protected HashMap<String, INTERNALSERIES> timeSeriesHolder;

	/**
	 * initialise the underlying time series holder
	 */
	public TimeSeriesCollection(){
		this.timeSeriesHolder = new HashMap<String,INTERNALSERIES>();
	}
	
	/**
	 * @param name
	 * @param series
	 * @throws IncompatibleTimeSeriesException
	 */
	public void addTimeSeries(String name, INTERNALSERIES series) throws IncompatibleTimeSeriesException {
		this.timeSeriesHolder.put(name, series);
		
	}
	
	/**
	 * @param name the name of the series
	 * @return the series held by the name
	 */
	public INTERNALSERIES series(String name){
		return this.timeSeriesHolder.get(name);
	}
	
	/**
	 * @return all the time series held
	 */
	public Collection<INTERNALSERIES> allseries(){
		return this.timeSeriesHolder.values();
	}
	

	@Override
	public TIMESERIES get(long time, int nbefore, int nafter) {
		TIMESERIES t = newInstance();
		HashMap<String, ALLINPUT> inputs = new HashMap<String,ALLINPUT>();
		long[] times = null;
		for (Entry<String, INTERNALSERIES> a : this.timeSeriesHolder.entrySet()) {
			INTERNALSERIES sub = a.getValue().get(time, nbefore, nafter);
			times = sub.getTimes();
			inputs.put(a.getKey(), sub.getData());
		}
		try{
			t.set(times, inputs);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return t;
	}
	
	@Override
	public Map<String, ALLINPUT> getData() {
		Map<String, ALLINPUT> ret = new HashMap<String, ALLINPUT>();
		for (Entry<String, INTERNALSERIES> es : this.timeSeriesHolder.entrySet()) {
			ret.put(es.getKey(), es.getValue().getData());
		}
		
		return ret;
	}
	
	@Override
	public TIMESERIES get(long start, long end) {
		TIMESERIES t = newInstance();
		HashMap<String, ALLINPUT> inputs = new HashMap<String,ALLINPUT>();
		long[] times = null;
		for (Entry<String, INTERNALSERIES> a : this.timeSeriesHolder.entrySet()) {
			INTERNALSERIES sub = a.getValue().get(start,end);
			times = sub.getTimes();
			inputs.put(a.getKey(), sub.getData());
		}
		try{
			t.set(times, inputs);
		}
		catch(Exception e){
			
		}
		return t;
	}
	@Override
	public TIMESERIES get(long time, int nbefore, int nafter, TIMESERIES output) {
		HashMap<String, ALLINPUT> inputs = new HashMap<String,ALLINPUT>();
		long[] times = null;
		for (Entry<String, INTERNALSERIES> a : this.timeSeriesHolder.entrySet()) {
			INTERNALSERIES sub = a.getValue().get(time,nbefore,nafter,output.timeSeriesHolder.get(a.getKey()));
			times = sub.getTimes();
			inputs.put(a.getKey(), sub.getData());
		}
		try{
			output.set(times, inputs);
		}
		catch(Exception e){
			
		}
		return output;
	}
	
	/**
	 * @return an instance of the internal series held
	 */
	public abstract INTERNALSERIES internalNewInstance();
	
	@Override
	public TIMESERIES get(long time, long threshbefore, long threshafter) {
		TIMESERIES t = newInstance();
		HashMap<String, ALLINPUT> inputs = new HashMap<String,ALLINPUT>();
		long[] times = null;
		for (Entry<String, INTERNALSERIES> a : this.timeSeriesHolder.entrySet()) {
			INTERNALSERIES sub = a.getValue().get(time,threshbefore,threshafter);
			times = sub.getTimes();
			inputs.put(a.getKey(), sub.getData());
		}
		try{
			t.set(times, inputs);
		}
		catch(Exception e){
			
		}
		return t;
	}
	
	/**
	 * @param names
	 * @return a new time series containing sub time series by name
	 */
	public TIMESERIES collectionByNames(String... names) {
		Map<String, ALLINPUT> ret = new HashMap<String, ALLINPUT>();
		ArrayList<long[]> times = new ArrayList<long[]>();
		for (String name: names) {
			INTERNALSERIES exists = this.timeSeriesHolder.get(name);
			if(exists != null) {
				ret.put(name, exists.getData());
				times.add(exists.getTimes());
			}
		}
		
		TIMESERIES rets = newInstance();
		Iterator<long[]> titer = times.iterator();
		try {
			rets.set(titer.next(), ret);
		} catch (TimeSeriesSetException e) {
		}
		return rets;
	}
	
	/**
	 * @param names
	 * @return a new time series containing sub time series by name
	 */
	public TIMESERIES collectionByNames(Collection<String> names){
		return collectionByNames(names.toArray(new String[names.size()]));
	}
	
	/**
	 * @return the set of names of the time series in this collection
	 */
	public Set<String> getNames(){
		Set<String> copy = new HashSet<String>();
		for (String string : this.timeSeriesHolder.keySet()) {
			copy.add(string);
		}
		return copy;
	}
	
	/**
	 * @return number of series held
	 */
	public int nSeries() {
		return this.timeSeriesHolder.size();
	}
	
	/**
	 * process the internal series held by this collection with this processor
	 * @param tsp
	 * @return a new instance of each internal series held in the same type of collection
	 */
	public TIMESERIES processInternal(TimeSeriesProcessor<ALLINPUT, SINGLEINPUT,INTERNALSERIES> tsp){
		TIMESERIES inst = newInstance();
		for (Entry<String, INTERNALSERIES> type: this.timeSeriesHolder.entrySet()) {
			try {
				inst.addTimeSeries(type.getKey(), type.getValue().process(tsp));
			} catch (IncompatibleTimeSeriesException e) {
			}
		}
		return inst;
	}
	
	/**
	 * process the internal series held by this collection with this processor
	 * @param tsp
	 * @return each held instance processed
	 */
	public TIMESERIES processInternalInplace(TimeSeriesProcessor<ALLINPUT, SINGLEINPUT,INTERNALSERIES> tsp){
		TIMESERIES inst = newInstance();
		for (Entry<String, INTERNALSERIES> type: this.timeSeriesHolder.entrySet()) {
			try {
				inst.addTimeSeries(type.getKey(), type.getValue().processInplace(tsp));
			} catch (IncompatibleTimeSeriesException e) {
			}
		}
		return inst;
	}
	
	/**
	 * @param <OUTPUTALL>
	 * @param <OUTPUTSINGLE>
	 * @param <OUTPUTTS>
	 * @param <OUTPUTTSC> 
	 * @param tsc the method to convert each item
	 * @param inst the instance to be filled
	 * @return the inputed instance for convenience 
	 */
	public <
		OUTPUTALL, 
		OUTPUTSINGLE, 
		OUTPUTTS extends TimeSeries<OUTPUTALL, OUTPUTSINGLE, OUTPUTTS>,
		OUTPUTTSC extends TimeSeriesCollection<OUTPUTALL, OUTPUTSINGLE, OUTPUTTSC, OUTPUTTS>> 
		OUTPUTTSC
		convertInternal(
			TimeSeriesConverter<ALLINPUT, SINGLEINPUT, INTERNALSERIES, OUTPUTALL, OUTPUTSINGLE, OUTPUTTS> tsc,
			OUTPUTTSC inst
		)
	{
		for (Entry<String, INTERNALSERIES> type: this.timeSeriesHolder.entrySet()) {
			try {
				inst.addTimeSeries(type.getKey(), type.getValue().convert(tsc));
			} catch (IncompatibleTimeSeriesException e) {
			}
		}
		return inst;
	}
	
	/**
	 * @param <OUTPUTALL>
	 * @param <OUTPUTSINGLE>
	 * @param <OUTPUTTS>
	 * @param <OUTPUTTSC> 
	 * @param tsc the method to convert each item
	 * @param tsp a processor to apply during conversion
	 * @param inst the instance to be filled
	 * @return the inputed instance for convenience 
	 */
	public <
		OUTPUTALL, 
		OUTPUTSINGLE, 
		OUTPUTTS extends TimeSeries<OUTPUTALL, OUTPUTSINGLE, OUTPUTTS>,
		OUTPUTTSC extends TimeSeriesCollection<OUTPUTALL, OUTPUTSINGLE, OUTPUTTSC, OUTPUTTS>> 
		OUTPUTTSC
		convertInternal(
			TimeSeriesConverter<ALLINPUT, SINGLEINPUT, INTERNALSERIES, OUTPUTALL, OUTPUTSINGLE, OUTPUTTS> tsc,
			TimeSeriesProcessor<OUTPUTALL, OUTPUTSINGLE,OUTPUTTS> tsp,
			OUTPUTTSC inst
		)
	{
		for (Entry<String, INTERNALSERIES> type: this.timeSeriesHolder.entrySet()) {
			try {
				inst.addTimeSeries(type.getKey(), type.getValue().convert(tsc,tsp));
			} catch (IncompatibleTimeSeriesException e) {
			}
		}
		return inst;
	}
	
	
	@Override
	public Iterator<IndependentPair<Long, Map<String, SINGLEINPUT>>> iterator() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public long[] getTimes() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public int size() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void set(long[] time, Map<String, ALLINPUT> data)throws TimeSeriesSetException {
		for (Entry<String, ALLINPUT> l : data.entrySet()) {
			INTERNALSERIES instance = internalNewInstance();
			instance.internalAssign(time,l.getValue());
			this.timeSeriesHolder.put(l.getKey(), instance);
		}
		
	}
}
