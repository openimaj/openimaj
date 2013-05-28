package org.openimaj.demos.sandbox.ml.linear.learner.stream;

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
import org.openimaj.demos.twitter.ContextRoundRobinTwitterSearchAPIDataset;
import org.openimaj.tools.twitter.modes.preprocessing.LanguageDetectionMode;
import org.openimaj.tools.twitter.modes.preprocessing.StopwordMode;
import org.openimaj.tools.twitter.modes.preprocessing.TokeniseMode;
import org.openimaj.twitter.USMFStatus;
import org.openimaj.util.api.auth.DefaultTokenFactory;
import org.openimaj.util.api.auth.common.TwitterAPIToken;
import org.openimaj.util.concurrent.ArrayBlockingDroppingQueue;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.context.ContextFunction;
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
		String[] tickers = new String[]{
			"apple","google","virgin","oracle","sony","microsoft"
		};
		ContextRealTimeWindowFunction<Map<String, Double>> yahooWindow = new ContextRealTimeWindowFunction<Map<String,Double>>(5000);
		Stream<Context> yahooAveragePriceStream = new YahooFinanceStream(true,tickers).transform(yahooWindow);

		List<Map<String,String>> geoLocs = loadGeoLocs("locations_input_srv_II.txt");

		// The Twitter Stream
		final ArrayBlockingDroppingQueue<Context> buffer = new ArrayBlockingDroppingQueue<Context>(1000);
		final LanguageDetectionMode languageDetectionMode = new LanguageDetectionMode();
		final StopwordMode stopwordMode = new StopwordMode();
		final TokeniseMode tokeniseMode = new TokeniseMode();

		Stream<Context> twitterUserWordCountStream =
			new ContextRoundRobinTwitterSearchAPIDataset(
				geoLocQueries(geoLocs),
				DefaultTokenFactory.get(TwitterAPIToken.class),
				buffer
			)
			.transform(new ContextRealTimeWindowFunction<Context>(10000))
			.map(
				new ContextListFunction<Context, Context>("item",
					new ContextTwitterStatusAsUSMFStatus()
				)
			)
			.map(
				new ContextListFunction<Context, Context>("item",
					new ContextFunction<USMFStatus,USMFStatus>("usmfstatus",
						new TwitterPreprocessingFunction(languageDetectionMode,tokeniseMode,stopwordMode)
					)
				)
		);
//		twitterUserWordCountStream.forEach(new Operation<Context>() {
//
//			@Override
//			public void perform(Context objectTime) {
//				List<Context> object = objectTime.getTyped("item");
//				for (Context statusContext : object) {
//					USMFStatus usmfStatus = statusContext.getTyped("usmfstatus");
//					System.out.format("@%s (location: %s): (geo: %s) (place: %s)\n",usmfStatus.user.name,usmfStatus.user.location,usmfStatus.geo,usmfStatus.location);
//				}
//			}
//
//		});
		List<ServerAddress> serverList = Arrays.asList(
			new ServerAddress("rumi",27017),
			new ServerAddress("hafez",27017)
		);
		StreamCombiner.combine(twitterUserWordCountStream,yahooAveragePriceStream)
		.forEach(
			new MongoDBOutputOp<
				IndependentPair<
					Context,
					Context
			>>
			(serverList) {

				@Override
				public String getCollectionName() {
					return "searchapi_yahoo_billgeo";
				}

				@Override
				public DBObject asDBObject(IndependentPair<Context,Context> obj) {
					BasicDBObject dbobj = new BasicDBObject();
					List<Context> tweets = obj.firstObject().getTyped("item");
					List<Object> dbtweets = new ArrayList<Object>();
					List<Object> normaltweets = new ArrayList<Object>();
					String actualQuery = null;
					HashMap<String,List<Integer>> queries = new HashMap<String,List<Integer>>();
					int item = 0;
					for (Context tweetContext : tweets) {
						USMFStatus usmfStatus = tweetContext.getTyped("usmfstatus");
						dbtweets.add(JSON.parse(usmfStatus.toJson()));
						normaltweets.add(JSON.parse((String)tweetContext.getTyped("status_json")));
						actualQuery = ((Query)tweetContext.getTyped("query")).getGeocode();
						List<Integer> itemMap = queries.get(actualQuery);
						if(itemMap == null) queries.put(actualQuery, itemMap  = new ArrayList<Integer>());
						itemMap.add(item++);
					}
					if(actualQuery!=null){
						dbobj.append("twitter_query", prepareQueries(queries));
					}
					dbobj.append("tweets", dbtweets);
					dbobj.append("tweets_raw", normaltweets);
					dbobj.append("search", "bill area code");
					List<?> stockTicks = obj.getSecondObject().getTyped("item");
					dbobj.append("tickers", stockTicks);
					long timestamp = obj.getSecondObject().getTyped("windowstart");
					dbobj.append("timestamp", timestamp);
					logger.debug(String.format("Dumping %d tweets and %d stock-ticks at %d with %d queries",dbtweets.size(),stockTicks.size(),timestamp,queries.size()));
					return dbobj;
				}

				private List<Map<String,Object>> prepareQueries(HashMap<String, List<Integer>> queries) {
					List<Map<String, Object>> ret = new ArrayList<Map<String,Object>>();
					for (Entry<String, List<Integer>> ql: queries.entrySet()) {
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("geoquery", ql.getKey());
						map.put("applies_to", ql.getValue());
						ret.add(map );
					}
					return ret ;
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
			String rawLocations = IOUtils.toString(GeoFinancialSearchAPIRecorder.class.getResourceAsStream(resource));
			String[] lines = rawLocations.split("\n");
			List<Map<String, String>> ret = new ArrayList<Map<String,String>>();
			for (String line : lines) {
				Map<String, String> location = new HashMap<String, String>();
				String[] fileLoc = line.split("/");
				String infoPart = fileLoc[fileLoc.length-1];
				String[] infoParts = infoPart.split(",");
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
		} catch (Throwable e) {
			return null;
		}
	}

	private static List<Query> geoLocQueries(List<Map<String,String>> geolocs){
		List<Query> ret = new ArrayList<Query>();
		for (Map<String, String> geoloc : geolocs) {
			Query q = new Query();
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
		return ret ;
	}
}