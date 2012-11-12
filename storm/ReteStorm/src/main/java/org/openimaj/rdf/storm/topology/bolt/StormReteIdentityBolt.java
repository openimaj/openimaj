package org.openimaj.rdf.storm.topology.bolt;

import java.util.Map;

import org.openimaj.rdf.storm.bolt.RETEStormNode;

import backtype.storm.tuple.Tuple;

import com.hp.hpl.jena.reasoner.rulesys.impl.RETERuleContext;

/**
 * Does nothing? nothing at all!
 * 
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class StormReteIdentityBolt extends StormReteBolt {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5721733139794446501L;

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
