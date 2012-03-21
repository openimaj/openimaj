package org.openimaj.demos.sandbox.twitter;

import java.io.IOException;

import org.openimaj.twitter.finance.YahooFinanceData;

public class YahooFinancePlayground {
	public static void main(String[] args) throws IOException {
		YahooFinanceData yd = new YahooFinanceData("AAPL", "10/10/2010", "11/10/2010","dd/MM/YYYY");
		yd.results();
	}
}
