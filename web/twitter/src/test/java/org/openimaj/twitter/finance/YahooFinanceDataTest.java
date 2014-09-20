/**
 * Copyright (c) 2012, The University of Southampton and the individual contributors.
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
package org.openimaj.twitter.finance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.junit.Test;
import org.openimaj.io.Cache;
import org.openimaj.ml.timeseries.processor.interpolation.LinearInterpolationProcessor;
import org.openimaj.ml.timeseries.series.DoubleTimeSeries;

import cern.colt.Arrays;

/**
 * A class which doesn't belong here, but I need it so here it lives!
 * 
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class YahooFinanceDataTest {

	/**
	 * @throws IOException
	 * 
	 */
	@Test
	public void testAPPLStock() throws IOException {
		final YahooFinanceData data = new YahooFinanceData("AAPL", "July 1 2010", "July 3 2010", "MMMM dd YYYY");
		System.out.println(data.resultsString());
		final Map<String, double[]> values = data.results();
		for (final Entry<String, double[]> iterable_element : values.entrySet()) {
			final String key = iterable_element.getKey();
			if (key.equals("Date")) {
				final DateTime t = new DateTime((long) iterable_element.getValue()[0]);
				assertEquals(t.getMonthOfYear(), DateTimeConstants.JULY);
			}
			System.out.println(key + ":");
			System.out.println(Arrays.toString(iterable_element.getValue()));
		}
	}

	/**
	 * @throws IOException
	 */
	@Test
	public void testTimeSeries() throws IOException {
		// Get time over a weekend
		final YahooFinanceData data = new YahooFinanceData("AAPL", "July 9 2010", "July 13 2010", "MMMM dd YYYY");
		assertEquals(data.timeperiods().length, 3);
		final long start = data.timeperiods()[0];
		final long end = data.timeperiods()[2];
		final DoubleTimeSeries series = data.seriesByName("High");
		final LinearInterpolationProcessor inter = new LinearInterpolationProcessor(start, end, 60l * 60 * 24 * 1000);
		inter.process(series);
		assertEquals(series.size(), 5);
	}

	/**
	 * Make sure the yahoo finance data can be cached properly
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCachedFinanceData() throws Exception {
		Cache.clear(YahooFinanceData.class, "AAPL", "July 9 2010", "July 13 2010", "MMMM dd YYYY");

		final YahooFinanceData fromAPI1 = new YahooFinanceData("AAPL", "July 9 2010", "July 13 2010", "MMMM dd YYYY");
		fromAPI1.results();
		assertTrue(fromAPI1.loadedFromAPI());

		final YahooFinanceData fromAPI2 = Cache.load(YahooFinanceData.class, "AAPL", "July 9 2010", "July 13 2010",
				"MMMM dd YYYY");
		fromAPI2.results();
		assertTrue(fromAPI2.loadedFromAPI());
		// assertTrue(fromAPI1.equals(fromAPI2));

		final YahooFinanceData fromCache1 = Cache.load(YahooFinanceData.class, "AAPL", "July 9 2010", "July 13 2010",
				"MMMM dd YYYY");
		fromCache1.results();
		assertTrue(!fromCache1.loadedFromAPI());
		// assertTrue(fromAPI1.equals(fromCache1));

		final YahooFinanceData fromAPI3 = Cache.loadSkipCache(YahooFinanceData.class, "AAPL", "July 9 2010",
				"July 13 2010", "MMMM dd YYYY");
		fromAPI3.results();
		assertTrue(fromAPI3.loadedFromAPI());
		// assertTrue(fromAPI1.equals(fromAPI3));

		Cache.clear(YahooFinanceData.class, "AAPL", "July 9 2010", "July 13 2010", "MMMM dd YYYY");

		final YahooFinanceData fromAPI4 = Cache.load(YahooFinanceData.class, "AAPL", "July 9 2010", "July 13 2010",
				"MMMM dd YYYY");
		fromAPI4.results();
		assertTrue(fromAPI4.loadedFromAPI());
		// assertTrue(fromAPI1.equals(fromAPI4));
	}
}
