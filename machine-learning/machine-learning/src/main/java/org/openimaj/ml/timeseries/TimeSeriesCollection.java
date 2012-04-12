package org.openimaj.ml.timeseries;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.openimaj.util.pair.IndependentPair;

/**
 * A collection of time series which share exactly the same time steps.
 * Given that this is true, SynchronisedTimeSeriesProcessor instances can perform interesting analysis
 *  
 * @author ss
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
	public Iterator<IndependentPair<Long, Map<String, SINGLEINPUT>>> iterator() {
		// TODO Auto-generated method stub
		return null;
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
	 * In some way flatten the held time series such that the output is:
	 * 
	 * @return [ALLDATAs1t1,ALLDATAs2t1,...,ALLDATAs1,t2,...,ALLDATAsntm] etc.
	 */
	public abstract ALLINPUT flatten();
	
	/**
	 * @return number of series held
	 */
	public int nSeries() {
		return this.timeSeriesHolder.size();
	}

}
