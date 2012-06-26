package org.openimaj.util.math;

/**
 * Implementing classes provide a set of simple arithmetic operators which can be
 * applied both inplace and on new object instances. These operators work on object types
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 * @param <OBJECT> The object type to be returned 
 */
public interface ObjectArithmetic<OBJECT> {
	/**
	 * Add to a copy of this.
	 * @param s add s to a copy of this
	 * @return a new instance of this 
	 */
	public OBJECT add(OBJECT s);
	
	/**
	 * Add to this.
	 * @param s add s to this
	 * @return this (for convenience)
	 */
	public OBJECT addInplace(OBJECT s);
	
	/**
	 * Subtract from a copy of this.
	 * @param s minus a copy of this by s
	 * @return a new instance of this 
	 */
	public OBJECT subtract(OBJECT s);
	
	/**
	 * Subtract from this.
	 * @param s minus this by s
	 * @return this (for convenience)
	 */
	public OBJECT subtractInplace(OBJECT s);
	
	/**
	 * Multiply a copy of this.
	 * @param s times a copy of this by s
	 * @return a new instance of this 
	 */
	public OBJECT multiply(OBJECT s);
	
	/**
	 * Multiply this.
	 * @param s times this by s
	 * @return this (for convenience)
	 */
	public OBJECT multiplyInplace(OBJECT s);
	
	/**
	 * Divide a copy of this.
	 * @param s divide a copy of this by s
	 * @return a new instance of this 
	 */
	public OBJECT divide(OBJECT s);
	
	/**
	 * Divide this.
	 * @param s divide this by s
	 * @return this (for convenience)
	 */
	public OBJECT divideInplace(OBJECT s);	
}
