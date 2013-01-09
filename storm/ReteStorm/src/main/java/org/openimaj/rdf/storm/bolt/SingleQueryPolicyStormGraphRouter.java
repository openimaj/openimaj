package org.openimaj.rdf.storm.bolt;

import java.util.List;

import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

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
		// TODO Auto-generated method stub
	}
	
	// INNER CLASSES
	
	public static class SQPESStormGraphRouter extends EddyStubStormGraphRouter {

		public SQPESStormGraphRouter(List<String> eddies) {
			super(eddies);
		}

		@Override
		protected void distributeToEddies(Tuple anchor, Values vals) {
			String source = anchor.getSourceComponent();
			if (this.eddies.contains(source))
				this.collector.emit(source, anchor, vals);
			else
				for (String eddy : this.eddies)
					this.collector.emit(eddy, anchor, vals);
			this.collector.ack(anchor);
		}
		
	}

}
