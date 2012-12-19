package org.openimaj.rdf.storm.bolt;

import java.util.Arrays;

import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import com.hp.hpl.jena.graph.Graph;

import org.openimaj.rdf.storm.bolt.StormGraphRouter;
import org.openimaj.rdf.storm.bolt.StormSteMBolt.Component;

/**
 * 
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 */
public class EddyStubStormGraphRouter extends StormGraphRouter {
	
	/**
	 * 
	 */
	public EddyStubStormGraphRouter(){
		
	}
	
	@Override
	protected long routingTimestamp(long stamp1, long stamp2){
		return stamp1 > stamp2 ? stamp1 : -1;
	}
	
	@Override
	public void routeGraph(Tuple anchor, boolean isAdd, Graph g, long... timestamp) {
		// The default assumption is that Tuple's from SteMs are intended for probing (i.e. NOT building).
		routeGraph(anchor, false, isAdd, g, timestamp);
	}

	@Override
	public void routeGraph(Tuple anchor, boolean isBuild, boolean isAdd, Graph g,
						   long... timestamp) {
		Values vals = new Values();
		for (Component c : Component.values()) {
			switch (c) {
			case isBuild:
				// set whether this Tuple is intended for probing or building into other SteMs
				vals.add(isBuild);
				break;
			case isAdd:
				// insert this Tuple's value of isAdd to be passed onto subscribing Bolts.
				vals.add(isAdd);
				break;
			case graph:
				// insert the new graph into the array of Values
				vals.add(g);
				break;
			case timestamp:
				vals.add(timestamp);
				break;
			default:
				break;
			}
		}
		
		this.collector.emit(StormEddyBolt.STREAM_TO_EDDY, anchor, vals);
		this.collector.ack(anchor);
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declareStream(StormEddyBolt.STREAM_TO_EDDY, new Fields( Arrays.asList( Component.strings() ) ) );
	}

}
