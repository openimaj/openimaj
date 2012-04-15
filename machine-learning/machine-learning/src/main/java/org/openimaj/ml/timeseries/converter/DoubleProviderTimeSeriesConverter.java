package org.openimaj.ml.timeseries.converter;

import org.openimaj.ml.timeseries.TimeSeries;
import org.openimaj.ml.timeseries.processor.TimeSeriesProcessor;
import org.openimaj.ml.timeseries.series.DoubleTimeSeries;
import org.openimaj.ml.timeseries.series.DoubleTimeSeriesProvider;


/**
 * @author ss
 *
 * @param <INPUTALL>
 * @param <INPUTSINGLE>
 * @param <INPUTTS>
 * 
 * Given a double provider, convert and process returning a {@link DoubleTimeSeries}
 */
public class DoubleProviderTimeSeriesConverter<
			INPUTALL,
			INPUTSINGLE,
			INPUTTS extends TimeSeries<INPUTALL,INPUTSINGLE,INPUTTS> & DoubleTimeSeriesProvider 
		>
		implements
		TimeSeriesConverter<INPUTALL, INPUTSINGLE, INPUTTS, double[], Double, DoubleTimeSeries> {

	@Override
	public DoubleTimeSeries convert(INPUTTS series) {
		return series.doubleTimeSeries();
	}

	@Override
	public DoubleTimeSeries convert(INPUTTS series,TimeSeriesProcessor<double[], Double, DoubleTimeSeries> processor) {
		DoubleTimeSeries r = series.doubleTimeSeries();
		processor.process(r);
		return r;
	}

}
