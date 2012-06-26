package org.openimaj.util.math;

/**
 * Implementing classes provide a set of simple arithmetic operators which can be
 * applied both in-place and on new object instances. These operators work on scalars.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @param <OBJECT> The object type to be returned 
 * @param <SCALAR> The scalar type to be added
 *
 */
public interface ScalarArithmetic<OBJECT, SCALAR extends Number> {
	/**
	 * Add to a copy of this.
	 * @param s add s to a copy of this
	 * @return a new instance of this 
	 */
	public OBJECT add(SCALAR s);
	
	/**
	 * Add to this.
	 * @param s add s to this
	 * @return this (for convenience)
	 */
	public OBJECT addInplace(SCALAR s);
	
	/**
	 * Subtract from a copy of this.
	 * @param s minus a copy of this by s
	 * @return a new instance of this 
	 */
	public OBJECT subtract(SCALAR s);
	
	/**
	 * Subtract from this.
	 * @param s minus this by s
	 * @return this (for convenience)
	 */
	public OBJECT subtractInplace(SCALAR s);
	
	/**
	 * Multiply a copy of this.
	 * @param s times a copy of this by s
	 * @return a new instance of this 
	 */
	public OBJECT multiply(SCALAR s);
	
	/**
	 * Multiply this.
	 * @param s times this by s
	 * @return this (for convenience)
	 */
	public OBJECT multiplyInplace(SCALAR s);
	
	/**
	 * Divide a copy of this.
	 * @param s divide a copy of this by s
	 * @return a new instance of this 
	 */
	public OBJECT divide(SCALAR s);
	
	/**
	 * Divide this.
	 * @param s divide this by s
	 * @return this (for convenience)
	 */
	public OBJECT divideInplace(SCALAR s);		
}
