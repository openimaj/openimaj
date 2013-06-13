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
package org.openimaj.ml.timeseries;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;
import org.openimaj.ml.timeseries.series.DoubleSynchronisedTimeSeriesCollection;
import org.openimaj.ml.timeseries.series.DoubleTimeSeries;

public class TimeSeriesCollectionTest {

	/**
	 * Tests a collection of time series
	 * 
	 * @throws TimeSeriesSetException
	 * @throws IncompatibleTimeSeriesException
	 */
	@Test
	public void testDoubleTimeSeries() throws TimeSeriesSetException, IncompatibleTimeSeriesException {
		final long[] times = new long[] { 1, 2, 5, 9, 10 };
		final double[] values1 = new double[] { 1, 2, 5, 9, 10 };
		final double[] values2 = new double[] { 2, 4, 10, 18, 20 };

		final DoubleTimeSeries series1 = new DoubleTimeSeries(times, values1);
		final DoubleTimeSeries series2 = new DoubleTimeSeries(times, values2);

		final DoubleSynchronisedTimeSeriesCollection dtsc = new DoubleSynchronisedTimeSeriesCollection();
		dtsc.addTimeSeries("normal", series1);
		dtsc.addTimeSeries("double", series2);

		final DoubleSynchronisedTimeSeriesCollection one = dtsc.get(1);
		assertTrue(one.flatten().length == 2);
		assertTrue(Arrays.equals(new double[] { 1, 2 }, one.flatten()));

		final DoubleSynchronisedTimeSeriesCollection two = dtsc.get(3, 1, 1);
		assertTrue(two.flatten().length == 4);
		assertTrue(Arrays.equals(new double[] { 2, 4, 5, 10 }, two.flatten()));

		final DoubleSynchronisedTimeSeriesCollection three = dtsc.get(5l, 3l, 4l);
		assertTrue(three.flatten().length == 6);
		assertTrue(Arrays.equals(new double[] { 2, 4, 5, 10, 9, 18 }, three.flatten()));
	}
}
