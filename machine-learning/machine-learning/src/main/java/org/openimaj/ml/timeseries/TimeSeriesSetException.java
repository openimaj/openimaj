package org.openimaj.ml.timeseries;

/**
 * Thrown if time series are set with insufficient times/dates
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class TimeSeriesSetException extends Exception {

	public TimeSeriesSetException(String string) {
		super(string);
	}

	public TimeSeriesSetException() {
		super("Time elements not equal to data specified");
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 8996396193077183355L;

}
