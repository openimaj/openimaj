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
package org.openimaj.image.pixel;


/**
 * 	Represents a pixel location. This is basically the same
 * 	as java.awt.Point except we can control exactly what goes
 * 	in here to optimise for memory usage.
 *
 *  @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *  
 *	
 */
public class IntValuePixel extends ValuePixel<Integer>
{
	/** The value of the pixel */
	public int value = 0;
	
	/**
	 * 	Default constructor
	 *	@param x X-location of the pixel
	 *	@param y Y-location of the pixel
	 */
	public IntValuePixel( int x, int y ) { super(x, y); }
	
	/**
	 * 	Constructor that takes the location and value of the
	 * 	pixel.
	 *	@param x X-location of the pixel
	 *	@param y Y-location of the pixel
	 *	@param v value of the pixel
	 */
	public IntValuePixel( int x, int y, int v ) { super(x, y); this.value = v; }
	
	/**
	 *	{@inheritDoc}
	 * 	@see java.lang.Object#toString()
	 */
	@Override
	public String toString() { return "{"+x+","+y+","+value+"}"; }

	@Override
	public Integer getValue() {
		return value;
	}
}

