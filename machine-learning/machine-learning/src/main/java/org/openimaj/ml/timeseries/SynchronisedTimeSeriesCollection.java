package org.openimaj.ml.timeseries;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A collection of time series which share exactly the same time steps.
 * Given that this is true, SynchronisedTimeSeriesProcessor instances can perform interesting analysis
 *  
 * @author ss
 * @param <ALLINPUT> The collection input type
 * @param <SINGLEINPUT> The type of a single time point data
 * @param <INTERNALSERIES> The type of the internal series held in this collection
 *
 */
public abstract class SynchronisedTimeSeriesCollection<
	ALLINPUT, 
	SINGLEINPUT, 
	INTERNALSERIES extends TimeSeries<ALLINPUT,SINGLEINPUT,INTERNALSERIES> 
> extends TimeSeriesCollection<
	ALLINPUT, 
	SINGLEINPUT,
	SynchronisedTimeSeriesCollection<ALLINPUT, SINGLEINPUT,INTERNALSERIES>,
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
	
	public void addTimeSeries(String name, INTERNALSERIES series) throws IncompatibleTimeSeriesException{
		if(this.time == null){
			this.time = series.getTimes();
		}
		else if(!Arrays.equals(this.time, series.getTimes())){
			throw new IncompatibleTimeSeriesException();
		}
		
		super.addTimeSeries(name, series);
	}
	
	public INTERNALSERIES series(String name){
		return this.timeSeriesHolder.get(name);
	}
	
	public Collection<INTERNALSERIES> allseries(){
		return this.timeSeriesHolder.values();
	}

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
	public void internalAssign(SynchronisedTimeSeriesCollection<ALLINPUT, SINGLEINPUT, INTERNALSERIES> interpolate) {
		this.time = interpolate.time;
		this.timeSeriesHolder = interpolate.timeSeriesHolder;
		
	}

	@Override
	public String toString() {
		return "Synchronised Time series";
	}
	
}
