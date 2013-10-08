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
package org.openimaj.demos.sandbox.ml.linear.learner.stream;

import java.net.UnknownHostException;
import java.util.List;

import org.openimaj.util.function.Operation;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

/**
 * Writes items from a stream of a mongodb instance
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @param <T>
 *
 */
public abstract class MongoDBOutputOp<T> implements Operation<T> {

	private MongoClient mongoClient;
	private DB db;
	private DBCollection collection;


	/**
	 * @throws UnknownHostException
	 *
	 */
	public MongoDBOutputOp() throws UnknownHostException {
		setup("localhost");
	}

	/**
	 * @param seeds
	 * @throws UnknownHostException
	 *
	 */
	public MongoDBOutputOp(List<ServerAddress> seeds) throws UnknownHostException {
		this.mongoClient = new MongoClient(seeds);
		this.db = mongoClient.getDB( getDBName() );
		this.collection = db.getCollection(getCollectionName());
	}

	private void setup(String host) throws UnknownHostException {
		this.mongoClient = new MongoClient( host);
		this.db = mongoClient.getDB( getDBName() );
		this.collection = db.getCollection(getCollectionName());
	}

	/**
	 * @return the database name
	 */
	public abstract String getDBName() ;

	/**
	 * @return the name of the collection to add items to
	 */
	public abstract String getCollectionName() ;

	@Override
	public void perform(T object) {
		DBObject asDBObject = asDBObject(object);
		this.collection.insert(asDBObject);
	}

	/**
	 * @param obj
	 * @return The DBObject to save to MongoDB
	 */
	public abstract DBObject asDBObject(T obj) ;

}
