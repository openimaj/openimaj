package org.openimaj.demos.sandbox.ml.linear.learner.stream;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.openimaj.ml.linear.evaluation.SumLossEvaluator;
import org.openimaj.ml.linear.learner.BilinearLearnerParameters;
import org.openimaj.ml.linear.learner.init.FirstValueInitStrat;
import org.openimaj.ml.linear.learner.init.SingleValueInitStrat;
import org.openimaj.ml.linear.learner.init.SparseZerosInitStrategy;
import org.openimaj.tools.twitter.modes.preprocessing.StopwordMode;
import org.openimaj.twitter.USMFStatus;
import org.openimaj.util.function.Operation;
import org.openimaj.util.pair.IndependentPair;

import com.mongodb.DBObject;
import com.mongodb.ServerAddress;
import com.mongodb.util.JSON;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class FinancialMongoStreamLearningExperiment {
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
		FirstValueInitStrat biasInitStrat = new FirstValueInitStrat();
		params.put(BilinearLearnerParameters.BIASINITSTRAT, biasInitStrat);

		List<ServerAddress> serverList = Arrays.asList(
				new ServerAddress("rumi",27017),
				new ServerAddress("hafez",27017)
			);
		// The combined stream
		new MongoDBQueryStream<IndependentPair<List<USMFStatus>,List<Map<String,Double>>>>(serverList) {

			@Override
			public String getCollectionName() {
				return "streamapi_yahoo";
			}

			@Override
			public String getDBName() {
				return "twitterticker";
			}

			@Override
			@SuppressWarnings("unchecked")
			public IndependentPair<List<USMFStatus>,List<Map<String,Double>>> constructObjects(DBObject next) {
				List<Map<String, Double>> ticks = (List<Map<String, Double>>) next.get("tickers");
				List<USMFStatus> tweets = new ArrayList<USMFStatus>();
				List<Object> objl = (List<Object>) next.get("tweets");
				for (Object object : objl) {
					USMFStatus status = new USMFStatus();
					status.fillFromString(JSON.serialize(object));
					tweets.add(status);
				}
				IndependentPair<List<USMFStatus>, List<Map<String, Double>>> ret =
						IndependentPair.pair(tweets, ticks);
				return ret ;
			}
		}
		.map(
				new CombinedStreamFunction<
					List<USMFStatus>,
					Map<String, Map<String, Double>>,
					List<Map<String,Double>>,
					Map<String, Double>>(
						new USMFStatusUserWordScore(new StopwordMode()),
						new WindowAverage()
				)
		)
		.map(
				new IncrementalLearnerWorldSelectingEvaluator(
					new SumLossEvaluator(),
					new IncrementalLearnerFunction(params)
				)
		)
		.forEach(new Operation<IndependentPair<List<String>,Double>>() {

			@Override
			public void perform(IndependentPair<List<String>, Double> object) {
				System.out.println("Loss: " + object.secondObject());
				System.out.println("Important words: " + object.firstObject());
			}
		});


	}
}