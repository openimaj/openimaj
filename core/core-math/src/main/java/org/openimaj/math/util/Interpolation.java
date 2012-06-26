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
package org.openimaj.math.util;

/**
 * Static methods for performing interpolation 
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class Interpolation {
	/**
	 * Linear interpolation of y at x given x0,y0 and x1,y1. 
	 * @param x the x position
	 * @param x0 the first x position
	 * @param y0 the first y position
	 * @param x1 the second x position
	 * @param y1 the second y position
	 * @return the interpolated value (y) at x
	 */
	public static float lerp(float x, float x0, float y0, float x1, float y1) {
		if (x == x0) return y0;
		return y0 + (x - x0)*((y1 - y0) / (x1 - x0));
	}
	
	/**
	 * Linear interpolation of y at x given x0,y0 and x1,y1. 
	 * @param x the x position
	 * @param x0 the first x position
	 * @param y0 the first y position
	 * @param x1 the second x position
	 * @param y1 the second y position
	 * @return the interpolated value (y) at x
	 */
	public static double lerp(double x, double x0, double y0, double x1, double y1) {
		if (x == x0) return y0;
		return y0 + (x - x0)*((y1 - y0) / (x1 - x0));
	}
	
	/**
	 * Bilinear interpolation of the value at x,y where 0<=x<=1 and 0<=y<=1
	 * given the values at (0,0), (0,1), (1,0) and (1,1). Note: This will work
	 * for other values of (x,y) but doesn't normally make sense.
	 * 
	 * @param x the x position (in 0..1)
	 * @param y the y position (in 0..1)
	 * @param f00 the value at (0,0)
	 * @param f01 the value at (0,1) 
	 * @param f10 the value at (1,0)
	 * @param f11 the value at (1,1)
	 * @return the interpolated value (x,y)
	 */
	public static double bilerp(double x, double y, double f00, double f01, double f10, double f11) {
		return f00*(1.0-x)*(1.0-y) + f10*x*(1.0-y) + f01*(1.0-x)*y + f11*x*y;
	}
	
	/**
	 * Bilinear interpolation of the value at x,y where 0<=x<=1 and 0<=y<=1
	 * given the values at (0,0), (0,1), (1,0) and (1,1). Note: This will work
	 * for other values of (x,y) but doesn't normally make sense.
	 * 
	 * @param x the x position (in 0..1)
	 * @param y the y position (in 0..1)
	 * @param f00 the value at (0,0)
	 * @param f01 the value at (0,1) 
	 * @param f10 the value at (1,0)
	 * @param f11 the value at (1,1)
	 * @return the interpolated value (x,y)
	 */
	public static float bilerpf(float x, float y, float f00, float f01, float f10, float f11) {
		return f00*(1.0f-x)*(1.0f-y) + f10*x*(1.0f-y) + f01*(1.0f-x)*y + f11*x*y;
	}
	
	/**
	 * Bilinear interpolation of the value at x,y where 0<=x<=1 and 0<=y<=1
	 * given the values at (0,0), (0,1), (1,0) and (1,1). Note: This will work
	 * for other values of (x,y) but doesn't normally make sense.
	 * 
	 * @param x the x position (in 0..1)
	 * @param y the y position (in 0..1)
	 * @param f00 the value at (0,0)
	 * @param f01 the value at (0,1) 
	 * @param f10 the value at (1,0)
	 * @param f11 the value at (1,1)
	 * @return the interpolated value (x,y)
	 */
	public static float bilerp(float x, float y, float f00, float f01, float f10, float f11) {
		return f00*(1.0f-x)*(1.0f-y) + f10*x*(1.0f-y) + f01*(1.0f-x)*y + f11*x*y;
	}
}
