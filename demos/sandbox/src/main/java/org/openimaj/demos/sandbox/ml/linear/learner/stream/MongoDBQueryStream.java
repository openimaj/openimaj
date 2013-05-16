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
