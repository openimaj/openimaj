package org.openimaj.demos.sandbox.ml.linear.learner.stream;

import java.net.UnknownHostException;

import org.openimaj.util.function.Operation;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

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
		this.mongoClient = new MongoClient( "localhost" );
		this.db = mongoClient.getDB( "mydb" );
		this.collection = db.getCollection(getCollectionName());
	}

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
