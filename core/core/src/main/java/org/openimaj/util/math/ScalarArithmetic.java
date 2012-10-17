/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
