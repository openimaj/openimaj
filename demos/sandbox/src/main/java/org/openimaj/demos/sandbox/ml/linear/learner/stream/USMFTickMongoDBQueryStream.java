package org.openimaj.demos.sandbox.ml.linear.learner.stream;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openimaj.twitter.USMFStatus;
import org.openimaj.util.data.Context;

import com.mongodb.DBObject;
import com.mongodb.ServerAddress;
import com.mongodb.util.JSON;

final class USMFTickMongoDBQueryStream extends MongoDBQueryStream<Context> {
	USMFTickMongoDBQueryStream(List<ServerAddress> seeds) throws UnknownHostException {
		super(seeds);
	}

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
	public Context constructObjects(DBObject next) {
		Context context = new Context();
		List<Map<String, Double>> ticks = (List<Map<String, Double>>) next.get("tickers");
		List<USMFStatus> tweets = new ArrayList<USMFStatus>();
		List<Object> objl = (List<Object>) next.get("tweets");
		for (Object object : objl) {
			USMFStatus status = new USMFStatus();
			status.fillFromString(JSON.serialize(object));
			tweets.add(status);
		}
		Long timestamp = (Long) next.get("timestamp");
		context.put("timestamp", timestamp);
		context.put("usmfstatuses",tweets);
		context.put("ticks",ticks);
		return context;
	}
}