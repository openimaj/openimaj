package org.openimaj.rdf.storm.bolt;

import backtype.storm.task.OutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;

import com.hp.hpl.jena.graph.Graph;

/**
 * 
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 */
public abstract class StormGraphRouter {
	
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
	
	/**
	 * 
	 * @param c
	 */
	public void setOutputCollector(OutputCollector c){
		this.collector = c;
	}
	
	protected abstract long routingTimestamp(long stamp1, long stamp2);
	
	/**
	 * 
	 * @param anchor 
	 * @param g
	 * @param isAdd
	 * @param newtimestamp
	 * @param oldtimestamp
	 */
	public void routeGraph(Tuple anchor, boolean isAdd, Graph g, long newtimestamp, long oldtimestamp){
		long ts = routingTimestamp(newtimestamp, oldtimestamp);
		if (ts >= 0)
			routeGraph(anchor, isAdd, g, ts);
	}
	
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
	 * @param isAdd
	 * @param timestamp
	 */
	public abstract void routeGraph(Tuple anchor, boolean isAdd, Graph g, long... timestamp);
	
	/**
	 * 
	 * @param anchor 
	 * @param g
	 * @param isBuild 
	 * @param isAdd
	 * @param timestamp
	 */
	public abstract void routeGraph(Tuple anchor, Action action, boolean isAdd, Graph g, long... timestamp);
	
	/**
	 * 
	 * @param declarer
	 */
	public abstract void declareOutputFields(OutputFieldsDeclarer declarer);
	
}
