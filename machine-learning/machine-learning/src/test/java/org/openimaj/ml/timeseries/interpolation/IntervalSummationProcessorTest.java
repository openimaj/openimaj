/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.ml.timeseries.interpolation;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openimaj.ml.timeseries.TimeSeriesArithmaticOperator;
import org.openimaj.ml.timeseries.processor.IntervalSummationProcessor;
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
		
		IntervalSummationProcessor<Double[],Double, DoubleTimeSeries> intervalSummationProcessor = new IntervalSummationProcessor<Double[],Double, DoubleTimeSeries>(new long[]{15,25,55});
		intervalSummationProcessor.process(dts);
		Double[] dtsD = dts.getData();
		assertArrayEquals(dtsD,new Double[]{20d,10d,30d});
	}	
	
	@Test
	public void testIntervalSummationError(){
		long[] times = new long[]{10,20,30,40,50};
		Double[] data = new Double[]{10d,10d,10d,10d,10d};
		
		DoubleTimeSeries dts = new DoubleTimeSeries();
		dts.set(times, data);
		
		IntervalSummationProcessor<Double[],Double, DoubleTimeSeries> intervalSummationProcessor = new IntervalSummationProcessor<Double[],Double, DoubleTimeSeries>(new long[]{0,5,9});
		DoubleTimeSeries dts1 = dts.copy();
		intervalSummationProcessor.process(dts1);
		Double[] dtsD = dts1.getData();
		assertArrayEquals(dtsD,new Double[]{0d,0d,0d});
		
		intervalSummationProcessor = new IntervalSummationProcessor<Double[],Double, DoubleTimeSeries>(new long[]{0,1,2,11,12});
		intervalSummationProcessor.process(dts);
		dtsD = dts.getData();
		assertArrayEquals(dtsD,new Double[]{0d,0d,0d,10d,0d});
		
	}
}
