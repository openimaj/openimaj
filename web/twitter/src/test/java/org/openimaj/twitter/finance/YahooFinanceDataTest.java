package org.openimaj.twitter.finance;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.joda.time.DateTime;
import org.junit.Test;

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
		YahooFinanceData data = new YahooFinanceData("AAPL","July 1 2010","July 10 2010", "MMMM dd YYYY");
		System.out.println(data.resultsString());
		Map<String, double[]> values = data.results();
		for (Entry<String,double[]> iterable_element : values.entrySet()) {
			System.out.println(iterable_element.getKey() + ":");
			System.out.println(Arrays.toString(iterable_element.getValue()));
		}
	}
}
