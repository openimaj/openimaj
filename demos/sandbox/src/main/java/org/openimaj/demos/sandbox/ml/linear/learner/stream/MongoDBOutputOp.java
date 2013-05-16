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
