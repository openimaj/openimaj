package org.openimaj.demos.sandbox.ml.linear.learner.stream.twitter;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.openimaj.demos.sandbox.ml.linear.learner.stream.MongoDBQueryStream;
import org.openimaj.tools.twitter.modes.preprocessing.CountryCodeMode;
import org.openimaj.tools.twitter.modes.preprocessing.TwitterPreprocessingMode;
import org.openimaj.twitter.USMFStatus;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.Operation;
import org.openimaj.util.function.context.ContextListFunction;

import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.json.DataObjectFactory;

import com.mongodb.DBObject;
import com.mongodb.ServerAddress;
import com.mongodb.util.JSON;

public class CountryCodeUSMFName {
	static {
		if(System.getProperty("os.name").toLowerCase().contains("mac")){

			ConsoleAppender console = new ConsoleAppender(); // create appender
			// configure the appender
			String PATTERN = "%d [%p|%c|%C{1}] %m%n";
			console.setLayout(new PatternLayout(PATTERN));
			console.setThreshold(Level.DEBUG);
			console.activateOptions();
			// add appender to any Logger (here is root)
			Logger.getRootLogger().addAppender(console);
		}
	}
	public static void main(String[] args) throws UnknownHostException {
		List<ServerAddress> servers = new ArrayList<ServerAddress>();
		servers.add(new ServerAddress("rumi"));
		servers.add(new ServerAddress("hafez"));
		final CountryCodeMode countryCodeMode = new CountryCodeMode();

		new MongoDBQueryStream<Context>(servers ) {

			@Override
			public String getCollectionName() {
				return "searchapi_yahoo_billgeo";
			}

			@Override
			public String getDBName() {
				return "twitterticker";
			}

			@Override
			public Context constructObjects(DBObject next) {
				Context ret = new Context();
				List<USMFStatus> tweets = new ArrayList<USMFStatus>();
				List<Status> raw = new ArrayList<Status>();
				@SuppressWarnings("unchecked")
				List<Object> objt = (List<Object>) next.get("tweets_raw");
				for (Object object : objt) {
					try {
						raw.add(DataObjectFactory.createStatus(JSON.serialize(object)));
					} catch (TwitterException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				List<Object> objl = (List<Object>) next.get("tweets");
				for (Object object : objl) {
					USMFStatus status = new USMFStatus();
					status.fillFromString(JSON.serialize(object));
					tweets.add(status);
				}
				ret.put("usmfstatuses", tweets);
				ret.put("tweets", raw);
				return ret ;
			}
		}
//		.forEach(new Operation<Context>() {
//
//			@Override
//			public void perform(Context object) {
//				List<Status> tweets = object.getTyped("tweets");
//				for (Status status : tweets) {
//					System.out.println(status);
//				}
//			}
//		});
		.map(new ContextListFunction<USMFStatus,USMFStatus>("usmfstatuses", new TwitterPreprocessingFunction(countryCodeMode)))
		.forEach(new Operation<Context>() {

			@Override
			public void perform(Context object) {
				List<USMFStatus> statuses = object.getTyped("usmfstatuses");
				List<String> places = new ArrayList<String>();
				for (USMFStatus usmfStatus : statuses) {
					try {
						places.add(TwitterPreprocessingMode.results(usmfStatus, countryCodeMode));
					} catch (Exception e) {
					}
				}
				System.out.println(places);
			}
		});
	}
}
