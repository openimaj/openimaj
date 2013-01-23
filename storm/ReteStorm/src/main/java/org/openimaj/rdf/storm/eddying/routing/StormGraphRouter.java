package org.openimaj.rdf.storm.eddying.routing;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.openimaj.rdf.storm.eddying.stems.StormSteMBolt.Component;

import backtype.storm.task.OutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import com.hp.hpl.jena.graph.Graph;

/**
 * 
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 */
public abstract class StormGraphRouter implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3433809982075329507L;

	/**
	 * 
	 * @author David Monks <dm11g08@ecs.soton.ac.uk>
	 */
	public static enum Action {

		/**
		 * 
		 */
		build
		,
		/**
		 *
		 */
		check
		,
		/**
		 *
		 */
		probe
		;
		private static String[] strings;
		static {
			Action[] vals = Action.values();
			strings = new String[vals.length];
			for (int i = 0; i < vals.length; i++) {
				strings[i] = vals[i].toString();
			}
		}

		/**
		 * @return like {@link #values()} but {@link String} instances
		 */
		public static String[] strings() {
			return strings;
		}
	}

	protected OutputCollector collector;
	
	protected abstract void prepare();
	
	/**
	 * 
	 * @param c
	 */
	public void setOutputCollector(OutputCollector c){
		this.collector = c;
		this.prepare();
	}
	
	protected abstract long routingTimestamp(long stamp1, long stamp2);
	
	/**
	 * 
	 * @param anchor 
	 * @param g
	 * @param isBuild 
	 * @param isAdd
	 * @param newtimestamp
	 * @param oldtimestamp
	 */
	public void routeGraph(Tuple anchor, Action action, boolean isAdd, Graph g, long newtimestamp, long oldtimestamp){
		long ts = routingTimestamp(newtimestamp, oldtimestamp);
		if (ts >= 0)
			routeGraph(anchor, action, isAdd, g, ts);
	}
	
	/**
	 * 
	 * @param anchor 
	 * @param g
	 * @param isBuild 
	 * @param isAdd
	 * @param timestamp
	 */
	public abstract void routeGraph(Tuple anchor, Action action, boolean isAdd, Graph g, long timestamp);
	
	/**
	 * 
	 * @param declarer
	 */
	public abstract void declareOutputFields(OutputFieldsDeclarer declarer);
	
	/**
	 * 
	 * @author David Monks <dm11g08@ecs.soton.ac.uk>
	 */
	public static abstract class EddyStubStormGraphRouter extends StormGraphRouter {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -3683430533570003022L;
		protected final List<String> eddies;
		
		/**
		 * 
		 * @param eddies 
		 * 			The list of eddies this router's SteM is part of.
		 */
		public EddyStubStormGraphRouter(List<String> eddies){
			this.eddies = eddies;
		}
		
		@Override
		protected long routingTimestamp(long stamp1, long stamp2){
			return stamp1 > stamp2 ? stamp1 : -1;
		}

		@Override
		public void routeGraph(Tuple anchor, Action action, boolean isAdd, Graph g,
							   long timestamp) {
			Values vals = new Values();
			for (Component c : Component.values()) {
				switch (c) {
				case action:
					// set whether this Tuple is intended for probing or building into other SteMs
					vals.add(action);
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
			
			distributeToEddies(anchor, vals);
		}
		
		protected abstract void distributeToEddies(Tuple anchor, Values vals);

		@Override
		public void declareOutputFields(OutputFieldsDeclarer declarer) {
			for (String eddy : this.eddies)
				declarer.declareStream(eddy, new Fields( Arrays.asList( Component.strings() ) ) );
		}

	}
	
}
