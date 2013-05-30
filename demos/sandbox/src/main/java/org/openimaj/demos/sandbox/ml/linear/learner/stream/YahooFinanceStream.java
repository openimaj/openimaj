package org.openimaj.demos.sandbox.ml.linear.learner.stream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.log4j.Logger;
import org.jboss.netty.handler.timeout.ReadTimeoutException;
import org.openimaj.io.HttpUtils;
import org.openimaj.io.HttpUtils.MetaRefreshRedirectStrategy;
import org.openimaj.util.function.Operation;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.util.stream.AbstractStream;

import com.Ostermiller.util.CSVParser;
import com.google.gson.Gson;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class YahooFinanceStream extends AbstractStream<Map<String,Double>>{
	Logger logger = Logger.getLogger(YahooFinanceStream.class);
	private static final String YAHOO_URI = "http://finance.yahoo.com/d/quotes.csv?s=%s&f=snl1";
	private static final String YAHOO_SUGGEST_URI = "http://d.yimg.com/autoc.finance.yahoo.com/autoc?query=%s&callback=YAHOO.Finance.SymbolSuggest.ssCallback";
	private static final int CONNECT_TIMEOUT = 1000;
	private static final int READ_TIMEOUT = 2000;
	private static final int ENFORCED_WAIT = 1000;
	private URL yahooURL;
	private long lastRead;
	private boolean expandTickers;

	private class FeedItem {
		String name;
		String longname;
		double value;
	}

	/**
	 * The tickers you want
	 * @param expand whether an attempt should be made to expand the tickers using #querySymbols
	 * @param tickers
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	public YahooFinanceStream(boolean expand, String ... tickers) throws MalformedURLException, IOException {
		if(expand) tickers = expandTickers(tickers);
		constructYahooURI(tickers);
		this.lastRead = 0;
	}

	private String[] expandTickers(String[] tickers) {
		Set<String> expanded = new HashSet<String>();
		for (String ticker : tickers) {
			try {
				List<Map<String, String>> queried = querySymbols(ticker);
				for (Map<String, String> query : queried) {
					if(query.containsKey("symbol")) expanded.add(query.get("symbol"));
				}
			} catch (IOException e) {
				// Just add the ticker back
				expanded.add(ticker);
			}
		}
		return expanded.toArray(new String[expanded.size()]);
	}

	/**
	 * The tickers you want
	 * @param tickers
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	public YahooFinanceStream(String ... tickers) throws MalformedURLException, IOException {
		constructYahooURI(tickers);
		this.lastRead = 0;
	}

	private void constructYahooURI(String[] tickers) throws MalformedURLException {
		for (int i = 0; i < tickers.length; i++) {
			try {
				tickers[i] = URLEncoder.encode(tickers[i], "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		String feeds = StringUtils.join(tickers, "+");
		String urlString = String.format(YAHOO_URI,feeds);
		this.yahooURL = new URL(urlString);

	}
	@Override
	public boolean hasNext() {
		return true;
	}

	@Override
	public Map<String,Double> next() {
		List<FeedItem> items = readItems();

		Map<String, Double> ret = new HashMap<String, Double>();
		for (FeedItem feedItem : items) {
			ret.put(feedItem.name,feedItem.value);
		}
		return ret ;
	}
	private List<FeedItem> readItems() {
		CSVParser parser;
		List<FeedItem> ret = new ArrayList<YahooFinanceStream.FeedItem>();
		if(System.currentTimeMillis() - this.lastRead < ENFORCED_WAIT){
			logger.debug(String.format("Haven't waited %d, waiting %d, sending old results",ENFORCED_WAIT,System.currentTimeMillis() - this.lastRead));
			try {
				Thread.sleep(ENFORCED_WAIT - (System.currentTimeMillis() - this.lastRead));
			} catch (InterruptedException e) {
			}
			return readItems();
		}
		try {
			IndependentPair<HttpEntity, ByteArrayInputStream> readURL = HttpUtils.readURLAsByteArrayInputStream(this.yahooURL, CONNECT_TIMEOUT, READ_TIMEOUT, new MetaRefreshRedirectStrategy() ,HttpUtils.DEFAULT_USERAGENT);
			parser = new CSVParser(readURL.secondObject());
			String[][] vals = parser.getAllValues();
			for (String[] strings : vals) {
				FeedItem feedItem = new FeedItem();
				feedItem.name = strings[0];
				feedItem.longname = strings[1];
				feedItem.value = Double.parseDouble(strings[2]);
				ret.add(feedItem);
			}
			logger.debug(String.format("Read succesfully!"));
			this.lastRead = System.currentTimeMillis();
		}
		catch (ConnectTimeoutException e){
			logger.debug("Connection timeout, sending old results");
			return this.readItems();
		}
		catch (ReadTimeoutException e){
			logger.debug("Read timeout!, sending old results");
			return this.readItems();
		}
		catch (IOException e) {
			return this.readItems();
		}
		return ret;
	}

	/**
	 * Access to the Yahoo! stock symbol suggestion API. For a plain text query returns possible stock ticker
	 * names in the format:
	 * [
	 * 	{symbol:, name:, exch:, type:, exchDisp:, typeDisp:},
	 *	...,
	 * ]
	 *
	 * @param query
	 * @return list of maps described above
	 * @throws IOException
	 */
	public static List<Map<String,String>> querySymbols(String query) throws IOException{
		Gson g = new Gson();
		URL queryURL = new URL(String.format(YAHOO_SUGGEST_URI,query));
		IndependentPair<HttpEntity, ByteArrayInputStream> readURL = HttpUtils.readURLAsByteArrayInputStream(queryURL, CONNECT_TIMEOUT, READ_TIMEOUT, new MetaRefreshRedirectStrategy() ,HttpUtils.DEFAULT_USERAGENT);

		String jsonp = IOUtils.toString(readURL.secondObject());
		String json = jsonp.substring(jsonp.indexOf("(") + 1, jsonp.lastIndexOf(")"));
		Map<?,?> parsed = g.fromJson(json, Map.class);

		try{
			@SuppressWarnings("unchecked")
			List<Map<String,String>> ret = (List<Map<String, String>>) ((Map<?,?>)parsed.get("ResultSet")).get("Result");
			return ret;
		}catch(Throwable c){
			throw new IOException(c);
		}
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		YahooFinanceStream yfs = new YahooFinanceStream(true,"google", "microsoft","yahoo");
		yfs.forEach(new Operation<Map<String,Double>>() {

			@Override
			public void perform(Map<String, Double> object) {
				for (Entry<String, Double> map : object.entrySet()) {
					System.out.println(map.getKey() + ": " + map.getValue());
				}
			}
		});
	}

}
