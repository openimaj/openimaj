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