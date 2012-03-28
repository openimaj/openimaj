package org.openimaj.ml.timeseries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;


/**
 * A time series defines data at discrete points in time. The time series has the ability to 
 * return data at a specific point in time, return neighbours within some window, 
 * closest neighbours or n neighbours before and after a time. 
 * 
 * These values can be used by a {@link TimeSeriesInterpolator} to get specific moments in time
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 * @param <DATA> the type of the data at each point in time
 *
 */
public class TimeSeries<DATA>{
	
	private TreeMap<Long,TimeSeriesData<DATA>> timeSeries;
	public TimeSeries(){
		timeSeries = new TreeMap<Long,TimeSeriesData<DATA>>();
	}
	
	/**
	 * Get the data at a specific moment in time. This may or may not be interpolated and may result in
	 * null if this time series has no value (or no means by which to interpolate a value) for the requested point
	 * of time
	 * 
	 * @param time
	 * @return the requested data or null.
	 */
	public DATA get(long time){
		TimeSeriesData<DATA> d = this.timeSeries.get(time);
		if(d==null)return null;
		return d.data;
	}
	
	/**
	 * returns the {@link TimeSeriesData} at a specific point in time and those before and after to the number 
	 * requested. This method may not return data for the specific requested time if it does not exists.
	 * If the time series is completely empty this function may return at an empty array however if at least 1
	 * data point exists and either nbefore and nafter are bigger than 1 then at least 1 datapoint will be returned.
	 * 
	 * Data should be returned in order
	 * 
	 * @param time
	 * @param nbefore
	 * @param nafter
	 * @return all data found with these parameters
	 */
	public LinkedList<DATA> get(long time, int nbefore, int nafter){
		LinkedList<DATA> timeSeriesList = new LinkedList<DATA>();
		addBefore(timeSeriesList,time,nbefore);
		addCurrent(timeSeriesList,time);
		addAfter(timeSeriesList,time,nafter);
		return timeSeriesList;
	}
	
	/**
	 * returns the {@link TimeSeriesData} at a specific point in time and those before and after within the specified
	 * thresholds. This method may not return data for the specific requested time if it does not exists.
	 * Similarly this method may return an empty array if no time data is available within the window specified.
	 * 
	 * Data should be returned in order
	 * 
	 * @param time
	 * @param threshbefore
	 * @param threshafter
	 * @return all data found with these parameters
	 */
	@SuppressWarnings("unchecked")
	public LinkedList<DATA> get(long time, long threshbefore, long threshafter){
		LinkedList<DATA> timeSeriesList = new LinkedList<DATA>();
		addBefore(timeSeriesList,time,threshbefore);
		addCurrent(timeSeriesList,time);
		addAfter(timeSeriesList,time,threshafter);
		return timeSeriesList;
	}
	
	private void addAfter(LinkedList<DATA> timeSeriesList, long time,long threshafter) {
		Entry<Long, TimeSeriesData<DATA>> ceilEntry = null;
		long maxTime = time + threshafter;
		long currentTime = time + 1; // Is this ok? The ceil should be either time + 1
		while((ceilEntry = this.timeSeries.ceilingEntry(currentTime)) != null){
			currentTime = ceilEntry.getKey();
			if(currentTime <= maxTime){
				timeSeriesList.addLast(ceilEntry.getValue().data);
				currentTime++; // we have to nudge it forward, ceil doesn't find the next one otherwise
			}
			else{
				break;
			}
		}// The entry was either null (didn't exist) or was beyond the threshold
		return;
	}

	private void addCurrent(LinkedList<DATA> timeSeriesList, long time) {
		DATA entry = this.get(time);
		if(entry != null) timeSeriesList.addLast(entry);
	}

	private void addBefore(LinkedList<DATA> timeSeriesList, long time,long threshbefore) {
		Entry<Long, TimeSeriesData<DATA>> floorEntry = null;
		long minTime = time - threshbefore;
		long currentTime = time;
		while((floorEntry = this.timeSeries.lowerEntry(currentTime)) != null){
			currentTime = floorEntry.getKey();
			if(currentTime >= minTime){
				timeSeriesList.addFirst(floorEntry.getValue().data);
			}
			else{
				break;
			}
		}// The entry was either null (didn't exist) or was beyond the threshold
		return;
	}
	
	private void addAfter(LinkedList<DATA> timeSeriesList, long time,int nafter) {
		Entry<Long, TimeSeriesData<DATA>> ceilEntry = null;
		int seen = 0;
		long currentTime = time;
		while((ceilEntry = this.timeSeries.higherEntry(currentTime )) != null){
			currentTime  = ceilEntry.getKey();
			if(seen < nafter){
				timeSeriesList.addLast(ceilEntry.getValue().data);
				seen ++;
			}
			else{
				break;
			}
		}// The entry was either null (didn't exist) or was beyond the threshold
		return;
	}


	private void addBefore(LinkedList<DATA> timeSeriesList, long time,int nbefore) {
		Entry<Long, TimeSeriesData<DATA>> floorEntry = null;
		int seen = 0;
		long currentTime = time;
		while((floorEntry = this.timeSeries.lowerEntry(currentTime)) != null){
			currentTime = floorEntry.getKey();
			if(seen < nbefore){
				timeSeriesList.addFirst(floorEntry.getValue().data);
				seen++;
			}
			else{
				break;
			}
		}// The entry was either null (didn't exist) or was beyond the threshold
		return;
	}

	/**
	 * Add to the time series
	 * @param toadd
	 */
	public void add(TimeSeriesData<DATA> toadd) {
		this.timeSeries.put(toadd.time, toadd);
	}
	
}
