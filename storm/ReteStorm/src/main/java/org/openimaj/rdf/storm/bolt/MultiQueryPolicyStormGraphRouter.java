package org.openimaj.rdf.storm.bolt;

import java.util.List;

import org.openimaj.rdf.storm.bolt.StormGraphRouter.EddyStubStormGraphRouter;

import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.graph.compose.MultiUnion;
import com.hp.hpl.jena.graph.compose.Polyadic;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class MultiQueryPolicyStormGraphRouter extends StormGraphRouter {
	
	private List<String> queries;
	
	/**
	 * @param qs
	 */
	public MultiQueryPolicyStormGraphRouter(List<String> qs){
		this.queries = qs;
		/*
		 * Separate the queries into their component triples, represented
		 * by filtered SteMs.
		 * 
		 * Decide which SteMs are part of which queries
		 */
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
	
	// INNER CLASSES
	
		public static class MQPESStormGraphRouter extends EddyStubStormGraphRouter {

			public MQPESStormGraphRouter(List<String> eddies) {
				super(eddies);
			}

			@Override
			protected void distributeToEddies(Tuple anchor, Values vals) {
				for (String eddy : this.eddies)
					this.collector.emit(eddy, anchor, vals);
			}
			
		}

}
