package org.openimaj.ml.timeseries.interpolation;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openimaj.ml.timeseries.TimeSeriesSetException;
import org.openimaj.ml.timeseries.interpolation.IntervalSummationProcessorTest.DoubleTimeSeries;
import org.openimaj.ml.timeseries.series.ConcreteTimeSeries;

public class IntervalSummationProcessorTest {
	
	class DoubleTimeSeries extends ConcreteTimeSeries<Double,DoubleTimeSeries> 
		implements TimeSeriesArithmaticOperator<Double, DoubleTimeSeries>{

		@Override
		public DoubleTimeSeries newInstance() {
			return new DoubleTimeSeries();
		}

		@Override
		public Double zero() {
			return 0d;
		}

		@Override
		public Double sum() {
			double t = 0;
			for (Double d: this.getData() ) {
				t+=d;
			}
			return t;
		}
		
	}
	
	@Test
	public void testIntervalSummation() {
		long[] times = new long[]{0,10,20,30,40,50};
		Double[] data = new Double[]{10d,10d,10d,10d,10d,10d};
		
		DoubleTimeSeries dts = new DoubleTimeSeries();
		dts.set(times, data);
		
		IntervalSummationProcessor<Double, DoubleTimeSeries> intervalSummationProcessor = new IntervalSummationProcessor<Double, DoubleTimeSeries>(new long[]{15,25,55});
		intervalSummationProcessor.process(dts);
		Double[] dtsD = dts.getData();
		assertArrayEquals(dtsD,new Double[]{20d,10d,30d});
	}	
}
