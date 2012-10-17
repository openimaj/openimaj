package org.openimaj.rdf.storm.bolt;

/**
 * 
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 */
public interface RETEStormSourceNode extends RETEStormNode {
	
	/**
	 * 
	 * @param sink
	 */
	public void setContinuation(RETEStormSinkNode sink);
	
}
