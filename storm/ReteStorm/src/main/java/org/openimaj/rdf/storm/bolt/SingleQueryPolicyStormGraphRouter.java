package org.openimaj.rdf.storm.bolt;

import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;

import com.hp.hpl.jena.graph.Graph;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class SingleQueryPolicyStormGraphRouter extends StormGraphRouter {
	
	private String query;
	
	/**
	 * @param q
	 */
	public SingleQueryPolicyStormGraphRouter(String q){
		this.query = q;
	}
	
	@Override
	protected long routingTimestamp(long stamp1, long stamp2){
		return stamp1 > stamp2 ? stamp1 : -1;
	}
	
	@Override
	public void routeGraph(Tuple anchor, boolean isAdd, Graph g, long... timestamp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void routeGraph(Tuple anchor, Action action, boolean isAdd, Graph g,
						   long... timestamp) {
		// TODO Auto-generated method stub

	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		
	}

}
