package org.openimaj.rdf.storm.sparql.topology.bolt;

import java.util.Map;

import org.openimaj.rdf.storm.bolt.RETEStormNode;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.reasoner.rulesys.impl.RETERuleContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;

public class StormSPARQLReteConflictSetIdentityBolt extends StormSPARQLReteConstructConflictSetBolt {
	
	private StormSPARQLReteConflictSetIdentityBolt(Query query) {
		super(query);
	}
	
	public static StormSPARQLReteConflictSetIdentityBolt construct(Query simpleQuery, StormSPARQLReteConflictSetBoltSink sink) {

		StormSPARQLReteConflictSetIdentityBolt toRet = null;
		if (simpleQuery.isSelectType()) {
			toRet = new StormSPARQLReteConflictSetIdentityBolt(simpleQuery);
		}
		else if (simpleQuery.isConstructType()) {
			toRet = new StormSPARQLReteConflictSetIdentityBolt(simpleQuery);
		}
		toRet.setSink(sink);
		return toRet;
	}
	
/**
	 * 
	 */
	private static final long serialVersionUID = -3035786591470164889L;


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

	@Override
	public void handleBinding(QueryIterator aggregatorRet) {
	}

}
