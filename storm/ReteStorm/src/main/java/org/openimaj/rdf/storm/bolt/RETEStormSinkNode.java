package org.openimaj.rdf.storm.bolt;

import backtype.storm.tuple.Values;

/**
 * 
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 */
public interface RETEStormSinkNode extends RETEStormNode {

	/**
	 * 
	 * @param output
	 * @param isAdd 
	 */
	public void fire (Values output, boolean isAdd);
	
	/**
	 * 
	 * @return boolean
	 */
	public boolean isActive ();
	
}
