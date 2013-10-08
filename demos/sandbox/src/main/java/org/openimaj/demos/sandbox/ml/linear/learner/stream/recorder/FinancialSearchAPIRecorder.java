/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
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
package org.openimaj.demos.sandbox.ml.linear.learner.stream.recorder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.openimaj.demos.sandbox.ml.linear.learner.stream.MongoDBOutputOp;
import org.openimaj.demos.sandbox.ml.linear.learner.stream.YahooFinanceStream;
import org.openimaj.demos.sandbox.ml.linear.learner.stream.twitter.TwitterPredicateFunction;
import org.openimaj.demos.sandbox.ml.linear.learner.stream.twitter.TwitterPreprocessingFunction;
import org.openimaj.demos.sandbox.ml.linear.learner.stream.twitter.TwitterStatusAsUSMFStatus;
import org.openimaj.stream.provider.twitter.TwitterSearchDataset;
import org.openimaj.tools.twitter.modes.filter.LanguageFilter;
import org.openimaj.tools.twitter.modes.preprocessing.LanguageDetectionMode;
import org.openimaj.tools.twitter.modes.preprocessing.StopwordMode;
import org.openimaj.tools.twitter.modes.preprocessing.TokeniseMode;
import org.openimaj.twitter.USMFStatus;
import org.openimaj.util.api.auth.DefaultTokenFactory;
import org.openimaj.util.api.auth.common.TwitterAPIToken;
import org.openimaj.util.concurrent.ArrayBlockingDroppingQueue;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.util.stream.Stream;
import org.openimaj.util.stream.combine.StreamCombiner;
import org.openimaj.util.stream.window.RealTimeWindowFunction;
import org.openimaj.util.stream.window.Window;
import org.openimaj.util.stream.window.WindowFilter;
import org.openimaj.util.stream.window.WindowFunction;

import twitter4j.Query;
import twitter4j.Status;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.ServerAddress;
import com.mongodb.util.JSON;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class FinancialSearchAPIRecorder {
	static Logger logger = Logger.getLogger(FinancialSearchAPIRecorder.class);
	/**
	 * @param args
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static void main(String[] args) throws MalformedURLException, IOException {

		// The financial stream
		String[] tickers = new String[]{
			"AAPL","GOOG","GE","GM","TWX"
		};
		RealTimeWindowFunction<Map<String,Double>> yahooWindow = new RealTimeWindowFunction<Map<String,Double>>(5000);
		Stream<Window<Map<String, Double>,Long>> yahooAveragePriceStream = new YahooFinanceStream(tickers).transform(yahooWindow);

		// The Twitter Stream
		final ArrayBlockingDroppingQueue<Status> buffer = new ArrayBlockingDroppingQueue<Status>(1);
		final LanguageDetectionMode languageDetectionMode = new LanguageDetectionMode();
		final StopwordMode stopwordMode = new StopwordMode();
		final TokeniseMode tokeniseMode = new TokeniseMode();

		final String queryStr = StringUtils.join(dollar(tickers), " OR ");
		Stream<Window<USMFStatus,Long>> twitterUserWordCountStream = new TwitterSearchDataset(
			new Query(queryStr),DefaultTokenFactory.get(TwitterAPIToken.class),buffer
		)
		.transform(new RealTimeWindowFunction<Status>(10000))
		.map(new WindowFunction<Status,USMFStatus,Long>(new TwitterStatusAsUSMFStatus()))
		.map(new WindowFunction<USMFStatus,USMFStatus,Long>(new TwitterPreprocessingFunction(languageDetectionMode,tokeniseMode,stopwordMode)))
		.map(new WindowFilter<USMFStatus,Long>(new TwitterPredicateFunction(new LanguageFilter("en"))));

//		twitterUserWordCountStream.forEach(new Operation<List<USMFStatus>>() {
//
//			@Override
//			public void perform(List<USMFStatus> object) {
//				for (USMFStatus usmfStatus : object) {
//					System.out.format("@%s: %s\n",usmfStatus.user.name,usmfStatus.text);
//				}
//			}
//		});
		List<ServerAddress> serverList = Arrays.asList(
			new ServerAddress("rumi",27017),
			new ServerAddress("hafez",27017)
		);
		StreamCombiner.combine(twitterUserWordCountStream,yahooAveragePriceStream)
		.forEach(
			new MongoDBOutputOp<
				IndependentPair<
					Window<USMFStatus,Long>,
					Window<Map<String,Double>,Long>
			>>
			(serverList) {

				@Override
				public String getCollectionName() {
					return "searchapi_yahoo";
				}

				@Override
				public DBObject asDBObject(IndependentPair<Window<USMFStatus,Long>,Window<Map<String,Double>,Long>> obj) {
					BasicDBObject dbobj = new BasicDBObject();
					List<USMFStatus> tweets = obj.firstObject().getPayload();
					List<Object> dbtweets = new ArrayList<Object>();
					for (USMFStatus usmfStatus : tweets) {
						dbtweets.add(JSON.parse(usmfStatus.toJson()));
					}
					dbobj.append("tweets", dbtweets);
					dbobj.append("search", queryStr);
					dbobj.append("tickers", obj.secondObject().getPayload());
					long timestamp = obj.secondObject().getMeta();
					dbobj.append("timestamp", timestamp);
					logger.debug(String.format("Dumping %d tweets and %d stock-ticks at %d",dbtweets.size(),obj.secondObject().getPayload().size(),timestamp));
					return dbobj;
				}

				@Override
				public String getDBName() {
					return "twitterticker";
				}
			}
		);
	}
	private static String[] dollar(String[] tickers) {
		String[] ret = new String[tickers.length];
		for (int i = 0; i < tickers.length; i++) {
			ret[i] = "$" + tickers[i];
		}
		return ret;
	}
}