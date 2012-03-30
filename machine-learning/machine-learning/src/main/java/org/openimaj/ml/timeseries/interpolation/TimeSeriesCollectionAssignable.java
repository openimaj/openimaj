package org.openimaj.ml.timeseries.interpolation;

import java.util.Collection;

import org.openimaj.ml.timeseries.TimeSeries;

/**
 * An object which can initialise a time series based on two java collections for time and data
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 * @param <DATA>
 * @param <TS>
 */
public interface TimeSeriesCollectionAssignable<DATA,TS extends TimeSeries<DATA[],TS>> {
	/**
	 * @param time
	 * @param data
	 * @return a new instance of the time series <TS> based on the two collections
	 */
	public TS newInstance(Collection<Long> time,Collection<DATA> data);
	
	/**
	 * Assign these values of data and time to the internal collection
	 * @param time
	 * @param data
	 */
	public void internalAssign(Collection<Long> time, Collection<DATA> data);
}
