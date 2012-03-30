package org.openimaj.twitter.finance;

import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.math.analysis.interpolation.LinearInterpolator;
import org.joda.time.DateTime;
import org.junit.Test;
import org.openimaj.ml.timeseries.interpolation.LinearTimeSeriesInterpolation;
import org.openimaj.ml.timeseries.series.DoubleTimeSeries;

import cern.colt.Arrays;

/**
 * A class which doesn't belong here, but I need it so here it lives!
 * 
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class YahooFinanceDataTest {
	
	/**
	 * @throws IOException 
	 * 
	 */
	@Test
	public void testAPPLStock() throws IOException{
		YahooFinanceData data = new YahooFinanceData("AAPL","July 1 2010","July 3 2010", "MMMM dd YYYY");
		System.out.println(data.resultsString());
		Map<String, double[]> values = data.results();
		for (Entry<String,double[]> iterable_element : values.entrySet()) {
			System.out.println(iterable_element.getKey() + ":");
			System.out.println(Arrays.toString(iterable_element.getValue()));
		}
	}
	
	/**
	 * @throws IOException
	 */
	@Test
	public void testTimeSeries() throws IOException{
		// Get time over a weekend
		YahooFinanceData data = new YahooFinanceData("AAPL","July 9 2010","July 13 2010", "MMMM dd YYYY");
		assertEquals(data.timeperiods().length,3);
		DoubleTimeSeries series = data.seriesByName("High");
		LinearTimeSeriesInterpolation inter = new LinearTimeSeriesInterpolation();
	}
}
