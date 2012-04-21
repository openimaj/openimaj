package org.openimaj.ml.timeseries.aggregator;

import java.util.Map;

import org.openimaj.math.matrix.MatrixUtils;
import org.openimaj.ml.timeseries.IncompatibleTimeSeriesException;
import org.openimaj.ml.timeseries.processor.interpolation.LinearInterpolationProcessor;
import org.openimaj.ml.timeseries.series.DoubleSynchronisedTimeSeriesCollection;
import org.openimaj.ml.timeseries.series.DoubleTimeSeries;
import org.openimaj.util.pair.IndependentPair;

import Jama.Matrix;

public class MeanSquaredDifferenceAggregator implements SynchronisedTimeSeriesCollectionAggregator<DoubleTimeSeries, DoubleSynchronisedTimeSeriesCollection, Double>{

	@Override
	public Double aggregate(DoubleSynchronisedTimeSeriesCollection series) {
		Matrix squarediffs = null;
		int size = 0;
		for (DoubleTimeSeries ds: series.allseries()) {
			if(squarediffs == null){
				squarediffs = new Matrix(new double[][]{ds.getData()});
			}
			else{
				squarediffs = squarediffs.minus(new Matrix(new double[][]{ds.getData()}));
				squarediffs = squarediffs.arrayTimes(squarediffs );
			}
			size = ds.size();
		}
		return MatrixUtils.sum(squarediffs)/size;
	}

	public static Double error(DoubleTimeSeries ... series) throws IncompatibleTimeSeriesException {
		DoubleTimeSeries first = series[0];
		long[] importantTimes = first.getTimes();
		DoubleSynchronisedTimeSeriesCollection aaplinterp = new DoubleSynchronisedTimeSeriesCollection();
		int i = 0;
		for (DoubleTimeSeries doubleTimeSeries : series) {
			if(i!=0){
				doubleTimeSeries = doubleTimeSeries.process(new LinearInterpolationProcessor(importantTimes));
			}
			aaplinterp.addTimeSeries("" + i++, doubleTimeSeries);
		}
		return new MeanSquaredDifferenceAggregator().aggregate(aaplinterp);
	}

}
