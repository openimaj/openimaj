package org.openimaj.twitter.finance;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.openimaj.io.ReadWriteableASCII;

import com.Ostermiller.util.CSVParser;

/**
 * A class which doesn't belong here, but I need it so here it lives!
 * 
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class YahooFinanceData implements ReadWriteableASCII{
	
	private final static String YAHOO_URL = "http://ichart.finance.yahoo.com/table.csv";
	private String product;
	private DateTime start;
	private DateTime end;
	private String data;
	private String[] titles;
	private Map<String, double[]> datavalues;
	private int nentries;
	
	/**
	 * @param product
	 * @param start
	 * @param end
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
		DateTimeFormatter parser= DateTimeFormat.forPattern("YYYY-MM-DD");
		while((line = creader.getLine()) != null){
			for (int i = 0; i < titles.length; i++) {
				String title = titles[i];
				if(i == 0){
					DateTime dt = parser.parseDateTime(line[i]);
					this.datavalues.get(title)[i] = dt.getMillis();
				}else{
					
					this.datavalues.get(title)[i] = Double.parseDouble(line[i]);
				}
			}
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
		uri.append(YAHOO_URL);
		uri.append("?s=").append(product);
		uri.append("&a=").append(start.getMonthOfYear());
		uri.append("&b=").append(start.getDayOfMonth());
		uri.append("&c=").append(start.getYear());
		uri.append("&d=").append(end.getMonthOfYear());
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
		this.nentries--; // the first line is titles
		return sb.toString();
	}
	
	private String doCall(String uri) throws IOException {
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
		while(in.hasNextLine()){
			this.data += in.nextLine() + "\n";
		}
		this.readData();
	}

	@Override
	public String asciiHeader() {
		return "YAHOO-FINANCE";
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		out.printf("%s %s %s\n",this.product,start.getMillis(),end.getMillis());
		out.println(this.data);
	}

	public long[] timeperiods() {
		DateTime begin = new DateTime(start);
		List<Long> times = new ArrayList<Long>();
		while(begin.isBefore(end)){
			times.add(begin.getMillis());
			begin.plusDays(1);
		}
		long[] out = new long[times.size()];
		int i = 0;
		for (long l : times) {
			out[i++] = l;
		}
		return out;
	}

	public Set<String> labels() {
		return this.datavalues.keySet();
	}
}
