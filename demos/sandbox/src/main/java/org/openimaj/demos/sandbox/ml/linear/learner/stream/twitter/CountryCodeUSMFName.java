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
		.map(new ContextListFunction<USMFStatus,USMFStatus>(new TwitterPreprocessingFunction(countryCodeMode), "usmfstatuses"))
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
