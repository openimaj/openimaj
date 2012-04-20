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
 * @author ss
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
	
	private long[] time;

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
