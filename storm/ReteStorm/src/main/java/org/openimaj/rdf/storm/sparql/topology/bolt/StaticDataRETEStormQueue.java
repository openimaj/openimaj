package org.openimaj.rdf.storm.sparql.topology.bolt;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.openimaj.rdf.storm.bolt.RETEStormQueue;
import org.openimaj.rdf.storm.bolt.RETEStormSinkNode;

import backtype.storm.tuple.Tuple;

import com.hp.hpl.jena.query.Query;

/**
 * An implementation of the {@link RETEStormQueue} which handles the querying of
 * static data stores using the query of the sibling bound with the variables
 * of this queue.
 * 
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class StaticDataRETEStormQueue extends RETEStormQueue {
	private static Logger logger = Logger.getLogger(StaticDataRETEStormQueue.class);
	private Query query;

	/**
	 * Constructor. The window is not usable until it has been bound
	 * to a sibling and a continuation node.
	 * 
	 * @param matchFields
	 *            Maps each field of the input tuple to the index of the
	 *            equivalent field in tuples from the other side of the join.
	 * @param outputFields
	 *            Maps each field of the output tuple to the index of the
	 *            equivalent field of the input tuple.
	 * @param size
	 * @param delay
	 * @param unit
	 * @param q
	 */
	public StaticDataRETEStormQueue(int[] matchFields,
			int[] outputFields,
			int size,
			long delay,
			TimeUnit unit, Query q) {
		super(matchFields, outputFields, size, delay, unit);
		this.query = q;
	}

	/**
	 * Constructor including sibling to bind to. The window is not usable until
	 * it has
	 * also been bound to a continuation node.
	 * 
	 * @param matchFields
	 *            Maps each field of the input tuple to the index of the
	 *            equivalent field in tuples from the other side of the join.
	 * @param outputFields
	 *            Maps each field of the output tuple to the index of the
	 *            equivalent field of the input tuple.
	 * @param size
	 * @param delay
	 * @param unit
	 * @param sib
	 * @param q
	 */
	public StaticDataRETEStormQueue(int[] matchFields,
			int[] outputFields,
			int size,
			long delay,
			TimeUnit unit,
			StaticDataRETEStormQueue sib, Query q) {
		super(matchFields, outputFields, size, delay, unit, sib);
		this.query = q;
	}

	/**
	 * Constructor including sibling to bind to. The window is not usable until
	 * it has
	 * also been bound to a continuation node.
	 * 
	 * @param matchFields
	 *            Maps each field of the input tuple to the index of the
	 *            equivalent field in tuples from the other side of the join.
	 * @param outputFields
	 *            Maps each field of the output tuple to the index of the
	 *            equivalent field of the input tuple.
	 * @param size
	 * @param delay
	 * @param unit
	 * @param sib
	 * @param sink
	 * @param q
	 */
	public StaticDataRETEStormQueue(int[] matchFields,
			int[] outputFields,
			int size,
			long delay,
			TimeUnit unit,
			StaticDataRETEStormQueue sib,
			RETEStormSinkNode sink, Query q) {
		super(matchFields, outputFields, size, delay, unit, sib, sink);
		this.query = q;
	}

	@Override
	public void fire(Tuple env, boolean isAdd, long timestamp) {
		logger.debug("\n This query: \n" + this.query + " \n " + "Sibling query: \n" + ((StaticDataRETEStormQueue) this.sibling).query);
		super.fire(env, isAdd, timestamp);
		// Now check the static data source
	}
}
