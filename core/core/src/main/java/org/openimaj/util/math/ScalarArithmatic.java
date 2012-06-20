package org.openimaj.util.math;

/**
 * Implementing classes provide a set of simple arithmetic operators which can be
 * applied both inplace and on new object instances. These operators work on scalars
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 * @param <OBJECT> The object type to be returned 
 * @param <SCALAR> The scalar type to be added
 *
 */
public interface ScalarArithmatic<OBJECT,SCALAR extends Number> {
	
	/**
	 * @param s add s to a copy of this
	 * @return a new instance of this 
	 */
	public OBJECT add(SCALAR s);
	/**
	 * @param s add s to this
	 * @return this (for convenience)
	 */
	public OBJECT addInplace(SCALAR s);
	
	/**
	 * @param s minus a copy of this by s
	 * @return a new instance of this 
	 */
	public OBJECT minus(SCALAR s);
	/**
	 * @param s minus this by s
	 * @return this (for convenience)
	 */
	public OBJECT minusInplace(SCALAR s);
	
	/**
	 * @param s times a copy of this by s
	 * @return a new instance of this 
	 */
	public OBJECT times(SCALAR s);
	/**
	 * @param s times this by s
	 * @return this (for convenience)
	 */
	public OBJECT timesInplace(SCALAR s);
	
	/**
	 * @param s divide a copy of this by s
	 * @return a new instance of this 
	 */
	public OBJECT divide(SCALAR s);
	/**
	 * @param s divide this by s
	 * @return this (for convenience)
	 */
	public OBJECT divideInplace(SCALAR s);
	
	
	
}
