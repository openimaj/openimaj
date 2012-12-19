package org.openimaj.rdf.storm.bolt;

import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;

import com.hp.hpl.jena.graph.Graph;

public class PolicyHoldingStormGraphRouter extends StormGraphRouter {
	
	public PolicyHoldingStormGraphRouter(){
		
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
	public void routeGraph(Tuple anchor, boolean isBuild, boolean isAdd, Graph g,
						   long... timestamp) {
		// TODO Auto-generated method stub

	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		
	}

}
