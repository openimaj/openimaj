package org.openimaj.ml.timeseries.aggregator;

import java.util.Map;

import org.openimaj.math.matrix.MatrixUtils;
import org.openimaj.ml.timeseries.series.DoubleSynchronisedTimeSeriesCollection;
import org.openimaj.ml.timeseries.series.DoubleTimeSeries;
import org.openimaj.util.pair.IndependentPair;

import Jama.Matrix;

public class SquaredSummedDifferenceAggregator implements SynchronisedTimeSeriesCollectionAggregator<DoubleTimeSeries, DoubleSynchronisedTimeSeriesCollection, Double>{

	@Override
	public Double aggregate(DoubleSynchronisedTimeSeriesCollection series) {
		Matrix squarediffs = null;
		for (DoubleTimeSeries ds: series.allseries()) {
			if(squarediffs == null){
				squarediffs = new Matrix(new double[][]{ds.getData()});
			}
			else{
				squarediffs = squarediffs.minus(new Matrix(new double[][]{ds.getData()}));
				squarediffs = squarediffs.arrayTimes(squarediffs );
			}
		}
		return MatrixUtils.sum(squarediffs);
	}

}
