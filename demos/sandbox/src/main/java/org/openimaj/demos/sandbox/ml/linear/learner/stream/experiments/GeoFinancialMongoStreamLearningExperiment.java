package org.openimaj.demos.sandbox.ml.linear.learner.stream.experiments;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.openimaj.demos.sandbox.ml.linear.learner.stream.IncrementalLearnerFunction;
import org.openimaj.demos.sandbox.ml.linear.learner.stream.IncrementalLearnerWorldSelectingEvaluator;
import org.openimaj.demos.sandbox.ml.linear.learner.stream.ModelStats;
import org.openimaj.demos.sandbox.ml.linear.learner.stream.StockPriceAggregator;
import org.openimaj.demos.sandbox.ml.linear.learner.stream.twitter.CountryCodeNameStrategy;
import org.openimaj.demos.sandbox.ml.linear.learner.stream.twitter.USMFStatusBagOfWords;
import org.openimaj.demos.sandbox.ml.linear.learner.stream.twitter.USMFTickMongoDBQueryStream;
import org.openimaj.ml.linear.evaluation.SumLossEvaluator;
import org.openimaj.ml.linear.learner.BilinearLearnerParameters;
import org.openimaj.ml.linear.learner.init.FirstValueInitStrat;
import org.openimaj.ml.linear.learner.init.SingleValueInitStrat;
import org.openimaj.ml.linear.learner.init.SparseZerosInitStrategy;
import org.openimaj.tools.twitter.modes.preprocessing.StopwordMode;
import org.openimaj.twitter.USMFStatus;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.Operation;
import org.openimaj.util.function.context.ContextFunctionAdaptor;
import org.openimaj.util.stream.window.WindowAverage;

import com.mongodb.ServerAddress;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class GeoFinancialMongoStreamLearningExperiment {
	/**
	 * @param args
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static void main(String[] args) throws MalformedURLException, IOException {
		BilinearLearnerParameters params = new BilinearLearnerParameters();
		params.put(BilinearLearnerParameters.ETA0_U, 0.02);
		params.put(BilinearLearnerParameters.ETA0_W, 0.02);
		params.put(BilinearLearnerParameters.LAMBDA, 0.001);
		params.put(BilinearLearnerParameters.BICONVEX_TOL, 0.01);
		params.put(BilinearLearnerParameters.BICONVEX_MAXITER, 10);
		params.put(BilinearLearnerParameters.BIAS, true);
		params.put(BilinearLearnerParameters.ETA0_BIAS, 0.5);
		params.put(BilinearLearnerParameters.WINITSTRAT, new SingleValueInitStrat(0.1));
		params.put(BilinearLearnerParameters.UINITSTRAT, new SparseZerosInitStrategy());
		params.put(BilinearLearnerParameters.EXPANDEDUINITSTRAT, new SparseZerosInitStrategy());
		params.put(BilinearLearnerParameters.EXPANDEDWINITSTRAT, new SingleValueInitStrat(0.05));
		FirstValueInitStrat biasInitStrat = new FirstValueInitStrat();
		params.put(BilinearLearnerParameters.BIASINITSTRAT, biasInitStrat);

		List<ServerAddress> serverList = Arrays.asList(
				new ServerAddress("rumi", 27017),
				new ServerAddress("hafez", 27017)
				);
		// Get the USMF status objects and financial ticks from the mongodb
		new USMFTickMongoDBQueryStream(serverList,"searchapi_yahoo_billgeo")
		// Transform the usmf status instances to bags of words
		.map(
			new ContextFunctionAdaptor<List<USMFStatus>, Map<String,Map<String,Double>>>(
				new USMFStatusBagOfWords(
					new StopwordMode(),
					new CountryCodeNameStrategy()
				),"usmfstatuses",
				"bagofwords"
			)
		)
		// transform the financial ticks to the average tick
		.map(
			new ContextFunctionAdaptor<List<Map<String,Double>>,Map<String,Double>>(new WindowAverage(),"ticks","averageticks")
		)
		// Group together identical stock ticks
		.transform(new StockPriceAggregator(0.0001))
		// Train the model
		.map(
			new IncrementalLearnerWorldSelectingEvaluator(
				new SumLossEvaluator(),
				new IncrementalLearnerFunction(params)
			)
		)
		// Consume the model statistics
		.forEach(new Operation<Context>() {

			@Override
			public void perform(Context context) {
				ModelStats object = context.getTyped("modelstats");
				object.printSummary();
			}
		});

	}
}
