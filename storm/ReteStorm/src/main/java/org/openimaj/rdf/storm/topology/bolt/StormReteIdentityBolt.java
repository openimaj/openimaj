package org.openimaj.rdf.storm.topology.bolt;

import java.util.Map;

import org.openimaj.rdf.storm.bolt.RETEStormNode;

import backtype.storm.tuple.Tuple;

import com.hp.hpl.jena.reasoner.rulesys.impl.RETERuleContext;

public class StormReteIdentityBolt extends StormReteBolt{


	@Override
	public RETEStormNode clone(Map<RETEStormNode, RETEStormNode> netCopy, RETERuleContext context) {
		return null;
	}

	@Override
	public void execute(Tuple input) {

	}

	@Override
	public void prepare() {
		// no prep required?
	}

	@Override
	public int getVariableCount() {
		// TODO Auto-generated method stub
		return 0;
	}

}
