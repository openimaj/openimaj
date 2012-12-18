package org.openimaj.rdf.storm.bolt;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openimaj.rdf.storm.topology.bolt.StormReteBolt.Component;

import com.hp.hpl.jena.reasoner.rulesys.impl.RETERuleContext;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public abstract class StormSteMBolt implements IRichBolt, RETEStormSinkNode {
	
	/**
	 * Meta-Data components of the storm values/fields list
	 *
	 * @author David Monks <dm11g08@ecs.soton.ac.uk>, Sina Samangooei
	 *         (ss@ecs.soton.ac.uk)
	 *
	 */
	public static enum Component {

		/**
		 * 
		 */
		isBuild
		,
		/**
		 *
		 */
		isAdd
		,
		/**
		 *
		 */
		graph
		,
		/**
		 *
		 */
		timestamp;
		private static String[] strings;
		static {
			Component[] vals = Component.values();
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
	
	public StormSteMBolt() {
		
	}

	private Map<String,Object> conf;
	private TopologyContext context;
	private OutputCollector collector;
	
	protected StormSteMQueue window;
	protected StormGraphRouter router;
	
	@SuppressWarnings("unchecked")
	@Override
	public void prepare(Map stormConf, TopologyContext context,
			OutputCollector collector) {
		this.conf = stormConf;
		this.context = context;
		this.collector = collector;
		
		this.window = new StormSteMQueue(0/*TODO*/, 5000, 10, TimeUnit.MINUTES, collector);
		this.window.setGraphRouter(new EddyStubStormGraphRouter());
	}

	@Override
	public void execute(Tuple input) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void fire(Values output, boolean isAdd) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fire(String streamID, Values output, boolean isAdd) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void emit(Tuple anchor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void emit(String streamID, Tuple anchor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isActive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void cleanup() {
		this.conf = null;
		this.context = null;
		this.collector = null;
		
		this.window = null;
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub

	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		return conf;
	}

	@Override
	public RETEStormNode clone(Map<RETEStormNode, RETEStormNode> netCopy,
			RETERuleContext context) {
		// TODO Auto-generated method stub
		return null;
	}

}
