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

import org.openimaj.util.stream.AbstractStream;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

/**
 * Encapsulates a MongoDB {@link DBCursor} instantiated from a {@link DBObject} query
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <T>
 */
public abstract class MongoDBQueryStream<T> extends AbstractStream<T>{
	private MongoClient mongoClient;
	private DB db;
	private DBCollection collection;
	private DBCursor cursor;

	/**
	 * @throws UnknownHostException
	 *
	 */
	public MongoDBQueryStream() throws UnknownHostException {
		setup("localhost");
		prepareCursor();
	}

	/**
	 * @param seeds
	 * @throws UnknownHostException
	 *
	 */
	public MongoDBQueryStream(List<ServerAddress> seeds) throws UnknownHostException {
		this.mongoClient = new MongoClient(seeds);
		this.db = mongoClient.getDB( getDBName() );
		this.collection = db.getCollection(getCollectionName());
		prepareCursor();
	}

	private void prepareCursor(){
		this.cursor = this.collection
					.find(getQuery(),getProjection())
					.sort(getSort())
					.limit(getLimit());
	}

	/**
	 * @return the limit of the query, defaults to 0
	 */
	public int getLimit() {
		return 0;
	}

	/**
	 * @return the data to project from the query
	 */
	public DBObject getProjection() {
		return new BasicDBObject();
	}

	/**
	 * @return how the query show be sorted
	 */
	public DBObject getSort() {
		return new BasicDBObject();
	}

	/**
	 * By Default returns a new and empty {@link BasicDBObject} meaning this
	 * stream goes through all documents in a given collection. Overwrite to alter
	 * query
	 * @return a {@link DBObject} query
	 */
	public DBObject getQuery() {
		return new BasicDBObject();
	}

	/**
	 * @return the name of the collection to query
	 */
	public abstract String getCollectionName() ;

	/**
	 * @return the name of the database to query
	 */
	public abstract String getDBName() ;

	private void setup(String host) throws UnknownHostException {
		this.mongoClient = new MongoClient( host);
		this.db = mongoClient.getDB( getDBName() );
		this.collection = db.getCollection(getCollectionName());
	}

	@Override
	public boolean hasNext() {
		return this.cursor.hasNext();
	}

	@Override
	public T next() {
		return constructObjects(this.cursor.next());
	}

	/**
	 * @param next
	 * @return construct the stream's object using the next document from the {@link DBCursor}
	 */
	public abstract T constructObjects(DBObject next) ;
}
