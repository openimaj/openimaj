package org.openimaj.hadoop.tools.twitter.token.mode.pointwisemi.count;

/**
 * Some statistics of pairs emitted
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public enum PairEnum {
	/**
	 * Number of pairs emitted
	 */
	PAIR,
	/**
	 * Number of unary counts emitted
	 */
	UNARY, 
	/**
	 * Number of pairs emitted by combiners
	 */
	PAIR_COMBINED,
	/**
	 * Number of unary counts emitted by combiners
	 */
	UNARY_COMBINED,

}
