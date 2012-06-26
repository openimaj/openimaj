package org.openimaj.ml.timeseries.series;

import java.util.Iterator;
import java.util.Map;

import org.openimaj.ml.timeseries.IncompatibleTimeSeriesException;
import org.openimaj.ml.timeseries.collection.SynchronisedTimeSeriesCollection;
import org.openimaj.util.pair.IndependentPair;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class DoubleSynchronisedTimeSeriesCollection extends SynchronisedTimeSeriesCollection<double[], Double, DoubleSynchronisedTimeSeriesCollection,DoubleTimeSeries>{
	
	/**
	 * basic constructor
	 */
	public DoubleSynchronisedTimeSeriesCollection() {
	}
	
	/**
	 * create a synchronised series from a bunch of pairs
	 * @param series
	 * @throws IncompatibleTimeSeriesException
	 */
	public DoubleSynchronisedTimeSeriesCollection(IndependentPair<String,DoubleTimeSeries> ... series) throws IncompatibleTimeSeriesException {
		for (IndependentPair<String, DoubleTimeSeries> dts : series) {
			this.addTimeSeries(dts.firstObject(), dts.secondObject());
		}
	}
	@Override
	public DoubleTimeSeries internalNewInstance() {
		return new DoubleTimeSeries();
	}

	@Override
	public DoubleSynchronisedTimeSeriesCollection newInstance() {
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

	@Override
	public Iterator<IndependentPair<Long, Map<String, Double>>> iterator() {
//		return new Iterator<IndependentPair<Long,Map<String,Double>>>() {
//		};
		return null;
	}
}
