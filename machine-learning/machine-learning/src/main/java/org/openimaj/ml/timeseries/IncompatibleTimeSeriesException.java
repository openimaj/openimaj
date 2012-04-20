package org.openimaj.ml.timeseries;

public class IncompatibleTimeSeriesException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public IncompatibleTimeSeriesException() {
	}
	
	public IncompatibleTimeSeriesException(String msg){
		super(msg);
	}
}
