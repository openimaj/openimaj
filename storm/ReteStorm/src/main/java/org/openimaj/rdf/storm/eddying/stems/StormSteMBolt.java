/**
 * Copyright (c) ${year}, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.rdf.storm.eddying.stems;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.openimaj.rdf.storm.eddying.routing.StormGraphRouter;
import org.openimaj.rdf.storm.eddying.routing.StormGraphRouter.Action;
import org.openimaj.rdf.storm.topology.bolt.StormReteBolt;

import com.hp.hpl.jena.graph.Graph;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class StormSteMBolt implements IRichBolt{
	
	private static final long serialVersionUID = -6233820433299486911L;
	private static Logger logger = Logger.getLogger(StormSteMBolt.class);

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
		action
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
	
	protected String name;
	protected StormGraphRouter router;
	
	protected int vars;
	protected int size;
	protected long delay;
	protected TimeUnit unit;
	
	/**
	 * Create a new SteM with fully customised characteristics.
	 * @param vars - variables available for matching
	 * @param size - capacity of the window
	 * @param delay - maximum age of graphs in the window
	 * @param unit - unit of time used
	 * @param name - the name of the SteM in the Storm topology.
	 * @param sgr - the graph routing object for use by the SteM.
	 */
	public StormSteMBolt(String name,
						 StormGraphRouter sgr,
						 int vars,
						 int size,
						 long delay,
						 TimeUnit unit) {
		this.name = name;
		this.router = sgr;
		this.vars = vars;
		this.size = size;
		this.delay = delay;
		this.unit = unit;
	}
	
	/**
	 * Create a new SteM with default queue characteristics:
	 * <ul>
	 * 	<li>variables available for matching: 3</li>
	 * 	<li>capacity of the window: 5000 graphs</li>
	 * 	<li>maximum age of graphs in the window: 10 units</li>
	 * 	<li>unit of time used: minutes</li>
	 * </ul>
	 * @param name - the name of the SteM in the Storm topology.
	 * @param sgr - the graph routing object for use by the SteM.
	 */
	public StormSteMBolt(String name, StormGraphRouter sgr) {
		this(name,sgr,3,5000,10,TimeUnit.MINUTES);
	}

	private Map<String,Object> conf;
	private TopologyContext context;
	private OutputCollector collector;
	
	protected StormSteMQueue window;
	
	@SuppressWarnings("unchecked")
	@Override
	public void prepare(@SuppressWarnings("rawtypes") Map stormConf,
						TopologyContext context,
						OutputCollector collector) {
		this.conf = stormConf;
		this.context = context;
		this.collector = collector;
		
		this.router.setOutputCollector(collector);
		
		this.window = new StormSteMQueue(vars, size, delay, unit, collector, router);
	}

	@Override
	public void execute(Tuple input) {
		long timestamp = input.getLongByField(Component.timestamp.toString());
		boolean isAdd = input.getBooleanByField(Component.isAdd.toString());
		switch ((Action)input.getValueByField(Component.action.toString())){
			case build:
				logger.debug(String.format("\nSteM %s building in triple: %s %s %s", this.name,
										   input.getValue(0).toString(),
										   input.getValue(1).toString(),
										   input.getValue(2).toString()));
				this.window.build(input, isAdd, timestamp);
				break;
			case check:
				this.window.check(input, isAdd, timestamp);
				break;
			case probe:
				logger.debug(String.format("\nSteM %s being probed with triple: %s %s %s", this.name,
						   				   input.getValue(0).toString(),
						   				   input.getValue(1).toString(),
						   				   input.getValue(2).toString()));
				this.window.probe(input, isAdd, timestamp);
				break;
			default:
		}
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
		this.router.declareOutputFields(declarer);
	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		return conf;
	}

}
