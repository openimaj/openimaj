package org.openimaj.rdf.storm.bolt;

import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;

import com.hp.hpl.jena.graph.Graph;

/**
 * 
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 */
public interface StormGraphRouter {

	/**
	 * 
	 * @param anchor 
	 * @param g
	 * @param isAdd
	 * @param timestamp
	 */
	public void routeGraph(Tuple anchor, boolean isAdd, Graph g, long timestamp);
	
	/**
	 * 
	 * @param anchor 
	 * @param g
	 * @param isBuild 
	 * @param isAdd
	 * @param timestamp
	 */
	public void routeGraph(Tuple anchor, boolean isBuild, boolean isAdd, Graph g, long timestamp);
	
	/**
	 * 
	 * @param declarer
	 */
	public void declareOutputFields(OutputFieldsDeclarer declarer);
	
}
