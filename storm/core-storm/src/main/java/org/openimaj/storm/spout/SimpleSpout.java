package org.openimaj.storm.spout;

import java.util.Map;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.base.BaseRichSpout;

/**
 * This class just overrides some methods from IRichSpout so that you don't need
 * to override them if you extend it.
 *
 * @author pere
 *
 */
@SuppressWarnings("rawtypes")
public abstract class SimpleSpout extends BaseRichSpout{

	/**
	 *
	 */
	private static final long serialVersionUID = -627422194685309431L;

	protected SpoutOutputCollector collector;
	protected TopologyContext context;
	protected Map conf;

	@Override
	public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
		this.conf = conf;
		this.context = context;
		this.collector = collector;
	}
}
