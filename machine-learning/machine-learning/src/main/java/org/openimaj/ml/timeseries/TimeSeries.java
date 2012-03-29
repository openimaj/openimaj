package org.openimaj.ml.timeseries;

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
public abstract class TimeSeries<DATA>{
	
	/**
	 * Same as calling {@link #get(time, 0, 0)} 
	 * 
	 * @param time
	 * @return the requested data or null.
	 */
	public DATA get(long time){
		return get(time,0,0);
	}

	
	/**
	 * returns the DATA at a specific point in time and those before and after to the number 
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
	public abstract DATA get(long time, int nbefore, int nafter);
	
	/**
	 * Same as {@link #get(long, int, int)} but instead of createing the output DATA instance, an existing data instance
	 * is handed which is filled. For convenience this output is also returned
	 * 
	 * Data should be returned in order
	 * 
	 * @param time
	 * @param nbefore
	 * @param nafter
	 * @param output 
	 * @return all data found with these parameters
	 */
	public abstract DATA get(long time, int nbefore, int nafter, DATA output);
	
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
	public abstract DATA get(long time, long threshbefore, long threshafter);
	
	
	/**
	 * Set the data associated with each time. This function explicitly assumes that time.length == data.length and there exists
	 * a single data instance per time instance
	 * 
	 * @param time instances of time
	 * @param data instances of data
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
	
}
