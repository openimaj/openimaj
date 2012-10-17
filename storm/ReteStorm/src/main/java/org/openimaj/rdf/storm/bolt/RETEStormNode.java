package org.openimaj.rdf.storm.bolt;

import java.util.Map;

import com.hp.hpl.jena.reasoner.rulesys.impl.RETERuleContext;

/**
 * 
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 */
public interface RETEStormNode {

	/**
     * Clone this node in the network across to a different context.
     * @param netCopy a map from RETENodes to cloned instance so far.
     * @param context the new context to which the network is being ported
	 * @return RETEStormNode
     */
    public RETEStormNode clone(Map<RETEStormNode, RETEStormNode> netCopy, RETERuleContext context) ;
	
}
