package org.openimaj.hadoop.tools.twitter.utils;

import org.openimaj.ml.timeseries.interpolation.TimeSeriesArithmaticOperator;
import org.openimaj.ml.timeseries.series.ConcreteTimeSeries;
import org.openimaj.ml.timeseries.series.DoubleTimeSeries;
import org.openimaj.ml.timeseries.series.DoubleTimeSeriesProvider;

/**
 * A time series of WordDFIDF instances
 * @author ss
 *
 */
public class WordDFIDFTimeSeries 
	extends ConcreteTimeSeries<WordDFIDF, WordDFIDFTimeSeries>
	implements 
		TimeSeriesArithmaticOperator<WordDFIDF, WordDFIDFTimeSeries>,
		DoubleTimeSeriesProvider
{

	@Override
	public WordDFIDFTimeSeries newInstance() {
		return new WordDFIDFTimeSeries();
	}

	@Override
	public WordDFIDF zero() {
		return new WordDFIDF();
	}

	
	/**
	 * An explicit assumption is made that {@link WordDFIDF} instances all
	 * come from the same period of time and therefore have the same total 
	 * number of tweets and total number of word instances across time 
	 * (i.e. {@link WordDFIDF#Ttf} and {@link WordDFIDF#Twf} remain untouched)
	 */
	@Override
	public WordDFIDF sum() {
		WordDFIDF ret = zero();
		for (WordDFIDF time : this.getData()) {
			ret.tf += time.tf;
			ret.wf += time.wf;
		}
		return ret;
	}

	@Override
	public DoubleTimeSeries doubleTimeSeries() {
		long[] times = this.getTimes();
		double[] values = new double[times.length];
		WordDFIDF[] current = this.getData();
		int i = 0;
		for (WordDFIDF wordDFIDF : current) {
			values[i++] = wordDFIDF.dfidf();
		}
		return new DoubleTimeSeries(times,values);
	}

}
