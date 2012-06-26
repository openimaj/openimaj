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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openimaj.io.CachableASCII;
import org.openimaj.ml.timeseries.processor.interpolation.LinearInterpolationProcessor;
import org.openimaj.ml.timeseries.processor.interpolation.util.TimeSpanUtils;
import org.openimaj.ml.timeseries.series.DoubleTimeSeries;

import com.Ostermiller.util.CSVParser;

/**
 * A class which doesn't belong here, but I need it so here it lives!
 * 
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class YahooFinanceData implements CachableASCII{
	
	private final static String YAHOO_URL = "http://ichart.finance.yahoo.com/table.csv";
	private String product;
	private DateTime start;
	private DateTime end;
	private String data;
	private String[] titles;
	private Map<String, double[]> datavalues;
	private int nentries;
	private boolean loadedFromAPICall = false;
	
	/**
	 * 
	 */
	public YahooFinanceData() {
	}
	
	/**
	 * Query the yahoo finance api for the product from the start date (inclusive) till the end date (inclusive)
	 * 
	 * @param product a stock ticker name e.g. AAPL
	 * @param start the start date
	 * @param end the end date
	 */
	public YahooFinanceData(String product, DateTime start, DateTime end){
		this.product = product;
		this.start = start;
		this.end = end;
	}
	
	/**
	 * @param product
	 * @param start
	 * @param end
	 * @param format yoda format
	 */
	public YahooFinanceData(String product, String start, String end, String format) {
		DateTimeFormatter parser= DateTimeFormat.forPattern(format);
		this.start = parser.parseDateTime(start);
		this.end = parser.parseDateTime(end);
		this.product = product;
	}

	private void prepare() throws IOException{
		if(this.data ==  null){
			String uri = buildURI(product, start, end);
			this.data = doCall(uri);
			this.loadedFromAPICall = true;
			readData();
		}
	}
	
	private void readData() throws IOException {
		
		StringReader reader = new StringReader(this.data);
		readData(reader);
	}

	private void readData(Reader in) throws IOException {
		CSVParser creader = new CSVParser(in);
		this.datavalues = new HashMap<String,double[]>();
		this.titles = creader.getLine();
		for (String title : titles) {
			this.datavalues.put(title, new double[nentries]);
		}
		String[] line = null;
		DateTimeFormatter parser= DateTimeFormat.forPattern("YYYY-MM-dd");
		int entry = nentries - 1;
		while((line = creader.getLine()) != null){
			for (int i = 0; i < titles.length; i++) {
				String title = titles[i];
				if(i == 0){
					DateTime dt = parser.parseDateTime(line[i]);
					this.datavalues.get(title)[entry ] = dt.getMillis();
				}else{
					
					this.datavalues.get(title)[entry ] = Double.parseDouble(line[i]);
				}
			}
			entry--;
		}
	}

	/**
	 * @return obtain the underlying data
	 * @throws IOException
	 */
	public String resultsString() throws IOException{
		prepare();
		return this.data;
	}
	
	/**
	 * @return obtain the underlying data
	 * @throws IOException
	 */
	public Map<String,double[]> results() throws IOException{
		prepare();
		return this.datavalues;
	}
	
	private String buildURI(String product, DateTime start, DateTime end) {
		StringBuilder uri = new StringBuilder();
		DateTime actualstart = start;
		uri.append(YAHOO_URL);
		uri.append("?s=").append(product);
		uri.append("&a=").append(actualstart.getMonthOfYear()-1);
		uri.append("&b=").append(actualstart.getDayOfMonth());
		uri.append("&c=").append(actualstart.getYear());
		uri.append("&d=").append(end.getMonthOfYear()-1);
		uri.append("&e=").append(end.getDayOfMonth());
		uri.append("&f=").append(end.getYear());
		uri.append("&g=d");
 
		return uri.toString();
	}
	
	private String responseToString(InputStream stream) throws IOException {
		BufferedInputStream bi = new BufferedInputStream(stream);
 
		StringBuilder sb = new StringBuilder();
 
		byte[] buffer = new byte[1024];
		int bytesRead = 0;
		this.nentries = 0;
		while ((bytesRead = bi.read(buffer)) != -1) {
			String s = new String(buffer, 0, bytesRead);
			for (char b : s.toCharArray()) {
				if(b == '\n') this.nentries++;
			}
			sb.append(s);
		}
		this.nentries--; 
		return sb.toString();
	}
	
	private String doCall(String uri) throws IOException {
		System.out.println("We're calling the uri");
		HttpClient httpClient = new HttpClient();
		httpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
		HttpMethod getMethod = new GetMethod(uri);
 
		try {
			int response = httpClient.executeMethod(getMethod);
 
			if (response != 200) {
				throw new IOException("HTTP problem, httpcode: "
						+ response);
			}
 
			InputStream stream = getMethod.getResponseBodyAsStream();
			String responseText = responseToString(stream);
			return responseText;
 
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
 
		return null;
	}

	@Override
	public void readASCII(Scanner in) throws IOException {	
		String[] inputParts = in.nextLine().split(" ");
		this.product = inputParts[0];
		this.start = new DateTime(Long.parseLong(inputParts[1]));
		this.end = new DateTime(Long.parseLong(inputParts[2]));
		this.nentries = Integer.parseInt(inputParts[3]);
		this.data = "";
		while(in.hasNextLine()){
			String l = in.nextLine();
			if(l.length() == 0) continue;
			this.data += l + "\n";
			
		}
		this.readData();
	}

	@Override
	public String asciiHeader() {
		return "YAHOO-FINANCE\n";
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		this.prepare();
		out.printf("%s %s %s %s\n",this.product,start.getMillis(),end.getMillis(),this.nentries);
		out.println(this.data);
	}

	/**
	 * @return the timeperiods actually retrieved 
	 * @throws IOException
	 */
	public long[] timeperiods() throws IOException {
		prepare();
		double[] dates = this.datavalues.get("Date");
		long[] times = new long[dates.length];
		int i = 0;
		for (double d : dates) {
			times[i++] = (long) d;
		}
		return times;
	}
	
	/**
	 * @param name
	 * @return stocks time series by name 
	 * @throws IOException
	 */
	public DoubleTimeSeries seriesByName(String name) throws IOException{
		prepare();
		if(!this.datavalues.containsKey(name))return null;
		return new DoubleTimeSeries(timeperiods(),this.datavalues.get(name));
	}
	
	/**
	 * @return stocks time series for each name
	 * @throws IOException
	 */
	public Map<String,DoubleTimeSeries> seriesMap() throws IOException{
		prepare();
		Map<String, DoubleTimeSeries> ret = new HashMap<String, DoubleTimeSeries>();
		long[] tp = this.timeperiods();
		for (Entry<String, double[]> namevalues : this.datavalues.entrySet()) {
			if(namevalues.getKey().equals("Date"))continue;
			ret.put(namevalues.getKey(), new DoubleTimeSeries(tp,namevalues.getValue()));
		}
		return ret;
	}
	
	/**
	 * @param times times to interpolate stocks to
	 * @return stocks time series for each name interpolated to the times
	 * @throws IOException
	 */
	public Map<String,DoubleTimeSeries> seriesMapInerp(long[] times) throws IOException{
		prepare();
		Map<String, DoubleTimeSeries> ret = new HashMap<String, DoubleTimeSeries>();
		LinearInterpolationProcessor interp = new LinearInterpolationProcessor(times);
		long[] tp = this.timeperiods();
		for (Entry<String, double[]> namevalues : this.datavalues.entrySet()) {
			if(namevalues.getKey().equals("Date")) continue;
			DoubleTimeSeries dt = new DoubleTimeSeries(tp,namevalues.getValue());
			interp.process(dt);
			ret.put(namevalues.getKey(), dt);
		}
		return ret;
	}

	/**
	 * @return all available data for each date
	 */
	public Set<String> labels() {
		return this.datavalues.keySet();
	}

	/**
	 * Interpolated finance results from the beggining time till the end in perscribed delta
	 * @param delta
	 * @return a map of stock components to time series
	 * @throws IOException
	 */
	public Map<String, DoubleTimeSeries> seriesMapInerp(long delta) throws IOException {
		long[] financeTimes = this.timeperiods();
		long start = financeTimes[0];
		long end = financeTimes[financeTimes.length-1];
		long[] times = TimeSpanUtils.getTime(start, end, delta);
		return seriesMapInerp(times);
	}

	@Override
	public String identifier() {
		DateTimeFormatter parser= DateTimeFormat.forPattern("YYYY-MM-dd");
		String startDate = this.start.toString(parser);
		String endDate = this.end.toString(parser);
		return String.format("%s-%s-%s",this.product,startDate,endDate);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof YahooFinanceData)) return false;
		YahooFinanceData that = (YahooFinanceData) obj;
		try {
			this.prepare();
			that.prepare();
		} catch (IOException e) {
			return false;
		}
		
		return this.data.equals(that.data); 
	}
	
	@Override
	public String toString() {
		return this.data;
	}

	/**
	 * @return Whether this data instance was actually loaded from the API or
	 * from a saved instance
	 */
	public boolean loadedFromAPI() {
		return this.loadedFromAPICall;
	}
}
