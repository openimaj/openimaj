package org.openimaj.rdf.storm.topology;

import java.util.ArrayList;
import java.util.Map;

import com.hp.hpl.jena.reasoner.rulesys.impl.BindingVector;
import com.hp.hpl.jena.reasoner.rulesys.impl.RETENode;
import com.hp.hpl.jena.reasoner.rulesys.impl.RETEQueue;
import com.hp.hpl.jena.reasoner.rulesys.impl.RETERuleContext;
import com.hp.hpl.jena.reasoner.rulesys.impl.RETESinkNode;

/**
 * Given the two parent bolt names, this bolt constructs two {@link RETEQueue}
 * instances. These instances are fed the output from the bolts as they arrive
 * and if a join satisfied their output is passed on.
 *
 * The internally held queues are where windows should be implemented
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class ReteJoinBolt implements RETESinkNode {

	/**
	 * @param leftBolt
	 * @param rightBolt
	 * @param matchIndices the variable indecies to watch out for
	 */
	public ReteJoinBolt(String leftBolt, String rightBolt, ArrayList<Byte> matchIndices) {
		RETEQueue leftQ = new RETEQueue(matchIndices);
        RETEQueue rightQ = new RETEQueue(matchIndices);
        leftQ.setSibling(rightQ);
        rightQ.setSibling(leftQ);
        leftQ.setContinuation(this);
	}

	@Override
	public RETENode clone(@SuppressWarnings("rawtypes") Map netCopy, RETERuleContext context) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void fire(BindingVector env, boolean isAdd) {
		// this is the signal to emit!
	}

}
