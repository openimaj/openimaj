package org.openimaj.rdf.storm.sparql.topology.bolt;

import java.util.Map;

import org.openimaj.rdf.storm.bolt.RETEStormNode;

import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.reasoner.rulesys.impl.RETERuleContext;

public class QueryHoldingReteFilterIdentityBolt extends QueryHoldingReteFilterBolt {
	
	public QueryHoldingReteFilterIdentityBolt() {
		super(null);
	}

/**
	 * 
	 */
	private static final long serialVersionUID = -3035786591470164889L;


	public QueryHoldingReteFilterIdentityBolt(Rule rule){
		super(rule);
	}
	
	@Override
	public RETEStormNode clone(Map<RETEStormNode, RETEStormNode> netCopy,
			RETERuleContext context) {
		return null;
	}

	@Override
	public void execute(Tuple input) {
		this.fire(new Values(), true);
		emit(input);
		acknowledge(input);
	}

	@Override
	public void prepare() {
	}
	
	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields());
	}

}
