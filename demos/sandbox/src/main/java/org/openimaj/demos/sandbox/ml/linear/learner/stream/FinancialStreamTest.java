package org.openimaj.demos.sandbox.ml.linear.learner.stream;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import org.openimaj.util.function.Operation;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class FinancialStreamTest {
	/**
	 * @param args
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static void main(String[] args) throws MalformedURLException, IOException {

		// The financial stream
		RealTimeWindowFunction<Map<String,Double>> yahooWindow = new RealTimeWindowFunction<Map<String,Double>>(1000);
		new YahooFinanceStream("AAPL","GOOG")
		.transform(yahooWindow)
		.map(new WindowAverage())
		.forEach(new Operation<Map<String,Double>>() {

			@Override
			public void perform(Map<String, Double> object) {
				System.out.println(object);
			}
		});


	}
}