package org.openimaj.demos.sandbox.ml.linear.learner.stream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.log4j.Logger;
import org.jboss.netty.handler.timeout.ReadTimeoutException;
import org.openimaj.io.HttpUtils;
import org.openimaj.io.HttpUtils.MetaRefreshRedirectStrategy;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.util.stream.AbstractStream;

import com.Ostermiller.util.CSVParser;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class YahooFinanceStream extends AbstractStream<Map<String,Double>>{
	Logger logger = Logger.getLogger(YahooFinanceStream.class);
	private static final String YAHOO_URI = "http://finance.yahoo.com/d/quotes.csv?s=%s&f=snl1";
	private static final int CONNECT_TIMEOUT = 1000;
	private static final int READ_TIMEOUT = 2000;
	private static final int ENFORCED_WAIT = 1000;
	private URL yahooURL;
	private long lastRead;

	private class FeedItem {
		String name;
		String longname;
		double value;
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
		String feeds = StringUtils.join(tickers, "+");
		this.yahooURL = new URL(String.format(YAHOO_URI,feeds));
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

}
