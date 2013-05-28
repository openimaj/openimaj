package org.openimaj.demos.sandbox.ml.linear.learner.stream;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import org.openimaj.util.function.Operation;
import org.openimaj.util.function.context.ContextFunction;
import org.openimaj.util.function.context.ContextOperation;
import org.openimaj.util.stream.window.ContextRealTimeWindowFunction;
import org.openimaj.util.stream.window.WindowAverage;

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
		ContextRealTimeWindowFunction<Map<String,Double>> yahooWindow = new ContextRealTimeWindowFunction<Map<String,Double>>(1000);
		new YahooFinanceStream(true,"apple","google")
		.transform(yahooWindow)
		.map(ContextFunction.func("item", "averageticks", new WindowAverage()))
		.forEach(ContextOperation.op("averageticks",
				new Operation<Map<String,Double>>() {
					@Override
					public void perform(Map<String, Double> object) {
						System.out.println(object);
					}
				}
			)
		);


	}
}