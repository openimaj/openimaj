package org.openimaj.ml.timeseries.series;

import org.openimaj.ml.timeseries.SynchronisedTimeSeriesCollection;

/**
 * @author ss
 *
 */
public class DoubleSynchronisedTimeSeriesCollection extends SynchronisedTimeSeriesCollection<double[], Double, DoubleTimeSeries>{

	@Override
	public DoubleTimeSeries internalNewInstance() {
		return new DoubleTimeSeries();
	}

	@Override
	public SynchronisedTimeSeriesCollection<double[], Double, DoubleTimeSeries> newInstance() {
		return new DoubleSynchronisedTimeSeriesCollection();
	}

	@Override
	public double[] flatten() {
		
		int tlength = this.getTimes().length;
		int nseries = this.nSeries();
		double[] flattened = new double[tlength * nseries];
		int seriesi = 0;
		for (DoubleTimeSeries series : this.allseries()) {
			double[] toCopy = series.getData();
			for (int timej = 0; timej < tlength; timej++) {
				flattened[seriesi + timej * nseries] = toCopy[timej];
			}
			seriesi++;
		}
		return flattened;
	}

}
