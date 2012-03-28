package org.openimaj.ml.timeseries;

/**
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 * @param <DATA>
 */
public class TimeSeriesData<DATA> implements Comparable<TimeSeriesData<DATA>>{
	/**
	 * the time of this time series data
	 */
	public long time;
	
	/**
	 * the data of this time series data
	 */
	public DATA data;

	/**
	 * Create a new data instance
	 * @param time
	 * @param data
	 */
	public TimeSeriesData(long time, DATA data) {
		this.time = time;
		this.data = data;
	}

	@Override
	public int compareTo(TimeSeriesData<DATA> o) {
		return new Long(this.time).compareTo(o.time);
	}
	
	/**
	 * Helper function, create new timeseries
	 * @param <DATA>
	 * @param time
	 * @param data
	 * @return
	 */
	public static <DATA> TimeSeriesData<DATA> create(long time, DATA data){
		return new TimeSeriesData<DATA>(time,data);
	}
}
