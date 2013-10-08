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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.openimaj.demos.sandbox.ml.linear.learner.stream.MongoDBOutputOp;
import org.openimaj.demos.sandbox.ml.linear.learner.stream.YahooFinanceStream;
import org.openimaj.demos.sandbox.ml.linear.learner.stream.twitter.ContextTwitterStatusAsUSMFStatus;
import org.openimaj.demos.sandbox.ml.linear.learner.stream.twitter.TwitterPreprocessingFunction;
import org.openimaj.stream.provider.twitter.ContextRoundRobinTwitterSearchAPIDataset;
import org.openimaj.tools.twitter.modes.preprocessing.CountryCodeMode;
import org.openimaj.tools.twitter.modes.preprocessing.LanguageDetectionMode;
import org.openimaj.tools.twitter.modes.preprocessing.StopwordMode;
import org.openimaj.tools.twitter.modes.preprocessing.TokeniseMode;
import org.openimaj.twitter.USMFStatus;
import org.openimaj.util.api.auth.DefaultTokenFactory;
import org.openimaj.util.api.auth.common.TwitterAPIToken;
import org.openimaj.util.concurrent.ArrayBlockingDroppingQueue;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.context.ContextFunctionAdaptor;
import org.openimaj.util.function.context.ContextListFunction;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.util.stream.Stream;
import org.openimaj.util.stream.combine.StreamCombiner;
import org.openimaj.util.stream.window.ContextRealTimeWindowFunction;

import twitter4j.GeoLocation;
import twitter4j.Query;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.ServerAddress;
import com.mongodb.util.JSON;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class GeoFinancialSearchAPIRecorder {
	static Logger logger = Logger.getLogger(GeoFinancialSearchAPIRecorder.class);

	/**
	 * @param args
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static void main(String[] args) throws MalformedURLException, IOException {

		// The financial stream
		final String[] tickers = new String[] {
				"apple", "google", "virgin", "oracle", "sony", "microsoft"
		};
		final ContextRealTimeWindowFunction<Map<String, Double>> yahooWindow = new ContextRealTimeWindowFunction<Map<String, Double>>(
				5000);
		final Stream<Context> yahooAveragePriceStream = new YahooFinanceStream(true, tickers).transform(yahooWindow);

		final List<Map<String, String>> geoLocs = loadGeoLocs("/org/openimaj/demos/sandbox/ml/linear/learner/stream/locations_input_srv_II.txt");

		// The Twitter Stream
		final ArrayBlockingDroppingQueue<Context> buffer = new ArrayBlockingDroppingQueue<Context>(1000);
		final LanguageDetectionMode languageDetectionMode = new LanguageDetectionMode();
		final StopwordMode stopwordMode = new StopwordMode();
		final TokeniseMode tokeniseMode = new TokeniseMode();
		final CountryCodeMode ccm = new CountryCodeMode();

		final Stream<Context> twitterUserWordCountStream =
				new ContextRoundRobinTwitterSearchAPIDataset(
						geoLocQueries(geoLocs),
						DefaultTokenFactory.get(TwitterAPIToken.class),
						buffer
				)
						.transform(new ContextRealTimeWindowFunction<Context>(10000))
						.map(
								new ContextListFunction<Context, Context>(new ContextTwitterStatusAsUSMFStatus(),
										"item"
								)
						)
						.map(
								new ContextListFunction<Context, Context>(new ContextFunctionAdaptor<USMFStatus, USMFStatus>("usmfstatus",
										new TwitterPreprocessingFunction(languageDetectionMode, tokeniseMode,
												stopwordMode)
								),
										"item"
								)
						);
		// twitterUserWordCountStream.forEach(new Operation<Context>() {
		//
		// @Override
		// public void perform(Context objectTime) {
		// List<Context> object = objectTime.getTyped("item");
		// for (Context statusContext : object) {
		// USMFStatus usmfStatus = statusContext.getTyped("usmfstatus");
		// System.out.format("@%s (location: %s): (geo: %s) (place: %s)\n",usmfStatus.user.name,usmfStatus.user.location,usmfStatus.geo,usmfStatus.location);
		// }
		// }
		//
		// });
		final List<ServerAddress> serverList = Arrays.asList(
				new ServerAddress("rumi", 27017),
				new ServerAddress("hafez", 27017)
				);
		StreamCombiner.combine(twitterUserWordCountStream, yahooAveragePriceStream)
				.forEach(
						new MongoDBOutputOp<
						IndependentPair<
						Context,
						Context
						>>
						(serverList)
						{

							@Override
							public String getCollectionName() {
								return "searchapi_yahoo_billgeo";
							}

							@Override
							public DBObject asDBObject(IndependentPair<Context, Context> obj) {
								final BasicDBObject dbobj = new BasicDBObject();
								final List<Context> tweets = obj.firstObject().getTyped("item");
								final List<Object> dbtweets = new ArrayList<Object>();
								final List<Object> normaltweets = new ArrayList<Object>();
								String actualQuery = null;
								final HashMap<String, List<Integer>> queries = new HashMap<String, List<Integer>>();
								int item = 0;
								for (final Context tweetContext : tweets) {
									final USMFStatus usmfStatus = tweetContext.getTyped("usmfstatus");
									dbtweets.add(JSON.parse(usmfStatus.toJson()));
									normaltweets.add(JSON.parse((String) tweetContext.getTyped("status_json")));
									actualQuery = ((Query) tweetContext.getTyped("query")).getGeocode();
									List<Integer> itemMap = queries.get(actualQuery);
									if (itemMap == null)
										queries.put(actualQuery, itemMap = new ArrayList<Integer>());
									itemMap.add(item++);
								}
								if (actualQuery != null) {
									dbobj.append("twitter_query", prepareQueries(queries));
								}
								dbobj.append("tweets", dbtweets);
								dbobj.append("tweets_raw", normaltweets);
								dbobj.append("search", "bill area code");
								final List<?> stockTicks = obj.getSecondObject().getTyped("item");
								dbobj.append("tickers", stockTicks);
								final long timestamp = (Long) obj.getSecondObject().getTyped("windowstart");
								dbobj.append("timestamp", timestamp);
								logger.debug(String.format("Dumping %d tweets and %d stock-ticks at %d with %d queries",
										dbtweets.size(), stockTicks.size(), timestamp, queries.size()));
								return dbobj;
							}

							private List<Map<String, Object>> prepareQueries(HashMap<String, List<Integer>> queries) {
								final List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
								for (final Entry<String, List<Integer>> ql : queries.entrySet()) {
									final Map<String, Object> map = new HashMap<String, Object>();
									map.put("geoquery", ql.getKey());
									map.put("applies_to", ql.getValue());
									ret.add(map);
								}
								return ret;
							}

							@Override
							public String getDBName() {
								return "twitterticker";
							}
						}
				);
	}

	private static List<Map<String, String>> loadGeoLocs(String resource) {
		try {
			final String rawLocations = IOUtils.toString(GeoFinancialSearchAPIRecorder.class
					.getResourceAsStream(resource));
			final String[] lines = rawLocations.split("\n");
			final List<Map<String, String>> ret = new ArrayList<Map<String, String>>();
			for (final String line : lines) {
				final Map<String, String> location = new HashMap<String, String>();
				final String[] fileLoc = line.split("/");
				final String infoPart = fileLoc[fileLoc.length - 1];
				final String[] infoParts = infoPart.split(",");
				location.put("country", infoParts[0]);
				location.put("city", infoParts[1]);
				location.put("lat", infoParts[2]);
				location.put("lon", infoParts[3]);
				location.put("rad", infoParts[4]);
				location.put("lang", infoParts[5]);
				location.put("group", infoParts[6]);
				ret.add(location);
			}
			return ret;
		} catch (final Throwable e) {
			return null;
		}
	}

	private static List<Query> geoLocQueries(List<Map<String, String>> geolocs) {
		final List<Query> ret = new ArrayList<Query>();
		for (final Map<String, String> geoloc : geolocs) {
			final Query q = new Query();
			q.geoCode(
					new GeoLocation(
							Double.parseDouble(geoloc.get("lat")),
							Double.parseDouble(geoloc.get("lon"))
					),
					Double.parseDouble(geoloc.get("rad")),
					Query.KILOMETERS
					);
			ret.add(q);
		}
		return ret;
	}
}
