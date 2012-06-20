package org.openimaj.util.math;

/**
 * Implementing classes provide a set of simple arithmetic operators which can be
 * applied both inplace and on new object instances. These operators work on object types
 * 
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 * @param <OBJECT> The object type to be returned 
 *
 */
public interface ObjectArithmatic<OBJECT> {
	
	/**
	 * @param s add s to a copy of this
	 * @return a new instance of this 
	 */
	public OBJECT add(OBJECT s);
	/**
	 * @param s add s to this
	 * @return this (for convenience)
	 */
	public OBJECT addInplace(OBJECT s);
	
	/**
	 * @param s minus a copy of this by s
	 * @return a new instance of this 
	 */
	public OBJECT minus(OBJECT s);
	/**
	 * @param s minus this by s
	 * @return this (for convenience)
	 */
	public OBJECT minusInplace(OBJECT s);
	
	/**
	 * @param s times a copy of this by s
	 * @return a new instance of this 
	 */
	public OBJECT times(OBJECT s);
	/**
	 * @param s times this by s
	 * @return this (for convenience)
	 */
	public OBJECT timesInplace(OBJECT s);
	
	/**
	 * @param s divide a copy of this by s
	 * @return a new instance of this 
	 */
	public OBJECT divide(OBJECT s);
	/**
	 * @param s divide this by s
	 * @return this (for convenience)
	 */
	public OBJECT divideInplace(OBJECT s);
	
	
	
}
