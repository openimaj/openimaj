package org.openimaj.rdf.storm.eddying.routing;

import java.util.List;

import org.apache.log4j.Logger;
import org.openimaj.rdf.storm.eddying.stems.StormSteMQueue;

import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import com.hp.hpl.jena.graph.Graph;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class SingleQueryPolicyStormGraphRouter extends StormGraphRouter {

	protected final static Logger logger = Logger.getLogger(SingleQueryPolicyStormGraphRouter.class);

	private String query;
	
	/**
	 * @param q
	 */
	public SingleQueryPolicyStormGraphRouter(String q){
		this.query = q;
	}
	
	protected void prepare(){
		
	}
	
	@Override
	protected long routingTimestamp(long stamp1, long stamp2){
		return stamp1 > stamp2 ? stamp1 : -1;
	}

	@Override
	public void routeGraph(Tuple anchor, Action action, boolean isAdd, Graph g,
						   long timestamp) {
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
		
		protected void prepare(){
			
		}

		@Override
		protected void distributeToEddies(Tuple anchor, Values vals) {
			String source = anchor.getSourceComponent();
			if (this.eddies.contains(source)){
				logger.debug(String.format("\nRouting back to Eddy %s for %s", source, vals.get(0).toString()));
				this.collector.emit(source, anchor, vals);
			}else
				for (String eddy : this.eddies){
					logger.debug(String.format("\nRouting on to Eddy %s for %s from %s", eddy, vals.get(0).toString(), source));
					this.collector.emit(eddy, anchor, vals);
				}
			this.collector.ack(anchor);
		}
		
	}

}
