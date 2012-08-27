package org.openimaj.rdf.storm.topology;

import backtype.storm.tuple.Tuple;

import com.hp.hpl.jena.graph.Triple;


/**
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class KestrelReteConflictSetBolt extends ReteConflictSetBolt {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2288621098852277643L;
	
	@Override
	protected void emitTriple(Tuple input, Triple t) {
		// Emit to the specific Kestrel queue
	}
	

}
