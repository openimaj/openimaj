package org.openimaj.demos.sandbox.ml.linear.learner.stream.twitter;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openimaj.demos.sandbox.ml.linear.learner.stream.MongoDBQueryStream;
import org.openimaj.twitter.USMFStatus;
import org.openimaj.util.data.Context;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.ServerAddress;
import com.mongodb.util.JSON;

/**
 * Get USMF statuses and financial ticks from a mongodb
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public final class USMFTickMongoDBQueryStream extends MongoDBQueryStream<Context> {
	private String collectionName;
	private String dbName;

	/**
	 * The query stream from the seeds (default collection and db)
	 * @param seeds
	 * @throws UnknownHostException
	 */
	public USMFTickMongoDBQueryStream(List<ServerAddress> seeds) throws UnknownHostException {
		super(seeds);
		this.collectionName = "streamapi_yahoo";
		this.dbName = "twitterticker";
	}

	/**
	 * The query stream from the seeds and a specific collection
	 * @param seeds
	 * @param collectionName
	 * @throws UnknownHostException
	 */
	public USMFTickMongoDBQueryStream(List<ServerAddress> seeds,String collectionName) throws UnknownHostException {
		super(seeds);
		this.collectionName = collectionName;
		this.dbName = "twitterticker";
	}

	@Override
	public String getCollectionName() {
		return this.collectionName;
	}

	@Override
	public DBObject getSort() {
		BasicDBObject sort = new BasicDBObject();
		sort.put("timestamp", 1);
		return sort;
	}

	@Override
	public String getDBName() {
		return this.dbName;
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