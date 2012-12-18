/**
 * Copyright (c) ${year}, The University of Southampton and the individual contributors.
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
package org.openimaj.rdf.storm.bolt;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.openimaj.rdf.storm.bolt.StormSteMBolt.Component;
import org.openimaj.rdf.storm.topology.bolt.StormReteBolt;
import org.openimaj.rdf.storm.topology.logging.LoggerBolt;
import org.openimaj.rdf.storm.utils.CircularPriorityWindow;

import backtype.storm.task.OutputCollector;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.compose.MultiUnion;
import com.hp.hpl.jena.graph.compose.Polyadic;
import com.hp.hpl.jena.reasoner.rulesys.impl.RETERuleContext;

/**
 * Represents one input left of a join node. The queue points to
 * a sibling queue representing the other leg which should be joined
 * against.
 * 
 * @author David Monks <dm11g08@ecs.soton.ac.uk>, based largely on the RETEQueue
 *         implementation by <a href="mailto:der@hplb.hpl.hp.com">Dave
 *         Reynolds</a>
 */
public class StormSteMQueue implements CircularPriorityWindow.DurationOverflowHandler<Tuple> {

	protected final static Logger logger = Logger.getLogger(RETEStormQueue.class);
	private static final boolean logging = false;
	private LoggerBolt.LogEmitter logStream;

	/** A time-prioritised and size limited sliding window of Tuples */
	private final CircularPriorityWindow<Tuple> window;

	/** The name of the stream over which this bolt acts */
//	private final String windowName;

	/** A count of {@link Fields} which should match between the two inputs */
	private int varCount;

	/**
	 * A set of {@link Fields} which should be produced by joins between the two
	 * inputs
	 */
//	protected final int[] outputIndices;

	/** The router that results should be passed on to */
	protected StormGraphRouter router;

	/**
	 * Constructor. The window is not usable until it has been bound
	 * to a sibling and a continuation node.
	 * @param vars 
	 * @param size
	 * @param delay
	 * @param unit
	 * @param oc 
	 */
	public StormSteMQueue(int vars,
						  int size,
						  long delay,
						  TimeUnit unit,
						  OutputCollector oc) {
		this.varCount = vars;
		this.window = new CircularPriorityWindow<Tuple>(this, size, delay, unit);
		if (logging)
			this.logStream = new LoggerBolt.LogEmitter(oc);
	}
	
	/**
	 * 
	 * @param sink
	 */
	public void setGraphRouter(StormGraphRouter sink) {
		this.router = sink;
	}

	/**
	 * Propagate a token to this node.
	 * 
	 * @param env
	 *            a set of variable bindings for the rule being processed.
	 * @param isBuild
	 * 			  distinguishes between build and probe operations
	 * @param isAdd
	 *            distinguishes between add and remove operations.
	 * @param timestamp
	 *            the time at which the triple was added from the stream
	 */
	public void fire(Tuple env, boolean isBuild, boolean isAdd, long timestamp) {
		if (isBuild) {
			if (isAdd)
				// Store the new token in this store
				this.window.offer(env);
			else
				// Remove any existing instances of the token from this store
				this.window.remove(env);
			this.router.routeGraph(env, isAdd,
								   (Graph) env.getValueByField(StormSteMBolt.Component.graph.toString()),
								   (Long) env.getValueByField(StormSteMBolt.Component.timestamp.toString()));
		} else {
			// Cross match new token against the entries in the sibling queue
			List<Object> values = env.getValues();
			logger.debug("\nChecking new tuple values: " + StormReteBolt.cleanString(values));
			logger.debug("Comparing new tuple to " + this.window.size() + " other tuples");
			for (Iterator<Tuple> i = this.window.iterator(); i.hasNext();) {
				Tuple candidate = i.next();
				boolean matchOK = true;
				for (int j = 0; j < this.varCount; j++) {
					// If the queue match indices indicate there should be a match get the
					// values of j in the queue and matchIndices[j] in the sibling
					Node thisNode = (Node) values.get(j);
					Node steMNode = (Node) candidate.getValue(j);
					if (!(thisNode.sameValueAs(steMNode) ||thisNode.isVariable())) {
						matchOK = false;
						break;
					}
				}
				if (matchOK) {
					logger.debug("Match Found! preparing for emit!");
					// Instantiate a new combined graph
					Graph g = joinSubGraphs(env, candidate);
	
					// initiate graph routing
					this.router.routeGraph(env, isAdd, g, timestamp);
				}
			}
		}
	}

	protected static Graph joinSubGraphs(Tuple thisTuple, Tuple steMTuple) {
		Polyadic newG = new MultiUnion();
		newG.addGraph((Graph) thisTuple.getValueByField(StormSteMBolt.Component.graph.toString()));
		newG.addGraph((Graph) steMTuple.getValueByField(StormSteMBolt.Component.graph.toString()));
		return newG;
	}

	@Override
	public void handleCapacityOverflow(Tuple overflow) {
		logger.debug("Window capacity exceeded.");
		if (logging)
			logStream.emit(new LoggerBolt.LoggedEvent(LoggerBolt.LoggedEvent.EventType.TUPLE_DROPPED,
													  overflow, "capacity"));
	}

	@Override
	public void handleDurationOverflow(Tuple overflow) {
		logger.debug("Tuple exceeded age of window.");
		if (logging)
			logStream.emit(new LoggerBolt.LoggedEvent(LoggerBolt.LoggedEvent.EventType.TUPLE_DROPPED,
													  overflow, "duration"));
		Values vals = new Values();
		vals.addAll(overflow.getValues());
//		this.continuation.fire("old", vals, true);
//		this.continuation.emit("old", overflow);
	}

}