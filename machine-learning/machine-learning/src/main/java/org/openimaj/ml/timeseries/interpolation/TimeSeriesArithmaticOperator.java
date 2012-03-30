package org.openimaj.ml.timeseries.interpolation;

import org.openimaj.ml.timeseries.TimeSeries;

/**
 * An object which defines a set of arithmatic operations of the represented time series
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 * @param <DATA>
 * @param <TS>
 */
public interface TimeSeriesArithmaticOperator<DATA, TS extends TimeSeries<DATA[],TS>> {
	
	/**
	 * @return a 0 data for this time series
	 */
	public DATA zero();
	
	/**
	 * add together all time series data elements
	 * @return Addition of all time series
	 */
	public DATA sum();
	
	
}
