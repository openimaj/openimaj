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
	 * 
	 * @param x
	 *            the x position
	 * @param x0
	 *            the first x position
	 * @param y0
	 *            the first y position
	 * @param x1
	 *            the second x position
	 * @param y1
	 *            the second y position
	 * @return the interpolated value (y) at x
	 */
	public static float lerp(float x, float x0, float y0, float x1, float y1) {
		if (x == x0)
			return y0;
		return y0 + (x - x0) * ((y1 - y0) / (x1 - x0));
	}

	/**
	 * Linear interpolation of y at x given x0,y0 and x1,y1.
	 * 
	 * @param x
	 *            the x position
	 * @param x0
	 *            the first x position
	 * @param y0
	 *            the first y position
	 * @param x1
	 *            the second x position
	 * @param y1
	 *            the second y position
	 * @return the interpolated value (y) at x
	 */
	public static double lerp(double x, double x0, double y0, double x1, double y1) {
		if (x == x0)
			return y0;
		return y0 + (x - x0) * ((y1 - y0) / (x1 - x0));
	}

	/**
	 * Cubic interpolation of y at x (in 0..1) given y at x=[-1, 0, 1, 2]
	 * 
	 * @param x
	 *            the x value to compute
	 * @param y0
	 *            y value at x=-1
	 * @param y1
	 *            y value at x=0
	 * @param y2
	 *            y value at x=1
	 * @param y3
	 *            y value at x=2
	 * @return the interpolated value
	 */
	public static double cubicInterp(double x, double y0, double y1, double y2, double y3) {
		return y1 + 0.5 * x * (y2 - y0 + x * (2.0 * y0 - 5.0 * y1 + 4.0 * y2 - y3 + x * (3.0 * (y1 - y2) + y3 - y0)));
	}

	/**
	 * Cubic interpolation of y at x (in 0..1) given y at x=[-1, 0, 1, 2]
	 * 
	 * @param x
	 *            the x value to compute
	 * @param y0
	 *            y value at x=-1
	 * @param y1
	 *            y value at x=0
	 * @param y2
	 *            y value at x=1
	 * @param y3
	 *            y value at x=2
	 * @return the interpolated value
	 */
	public static float cubicInterp(float x, float y0, float y1, float y2, float y3) {
		return (float) (y1 + 0.5 * x
				* (y2 - y0 + x * (2.0 * y0 - 5.0 * y1 + 4.0 * y2 - y3 + x * (3.0 * (y1 - y2) + y3 - y0))));
	}

	/**
	 * Cubic interpolation of y at x (in 0..1) given y at x=[-1, 0, 1, 2]
	 * 
	 * @param x
	 *            the x value to compute
	 * @param y
	 *            an array of 4 y values at x=[-1, 0, 1, 2]
	 * @return the interpolated value
	 */
	public static double cubicInterp(double x, double[] y) {
		return y[1] + 0.5 * x * (y[2] - y[0] + x
				* (2.0 * y[0] - 5.0 * y[1] + 4.0 * y[2] - y[3] + x * (3.0 * (y[1] - y[2]) + y[3] - y[0])));
	}

	/**
	 * Cubic interpolation of y at x (in 0..1) given y at x=[-1, 0, 1, 2]
	 * 
	 * @param x
	 *            the x value to compute
	 * @param y
	 *            an array of 4 y values at x=[-1, 0, 1, 2]
	 * @return the interpolated value
	 */
	public static float cubicInterp(float x, float[] y) {
		return (float) (y[1] + 0.5 * x * (y[2] - y[0] + x
				* (2.0 * y[0] - 5.0 * y[1] + 4.0 * y[2] - y[3] + x * (3.0 * (y[1] - y[2]) + y[3] - y[0]))));
	}

	/**
	 * Bilinear interpolation of the value at x,y where 0<=x<=1 and 0<=y<=1
	 * given the values at (0,0), (0,1), (1,0) and (1,1). Note: This will work
	 * for other values of (x,y) but doesn't normally make sense.
	 * 
	 * @param x
	 *            the x position (in 0..1)
	 * @param y
	 *            the y position (in 0..1)
	 * @param f00
	 *            the value at (0,0)
	 * @param f01
	 *            the value at (0,1)
	 * @param f10
	 *            the value at (1,0)
	 * @param f11
	 *            the value at (1,1)
	 * @return the interpolated value (x,y)
	 */
	public static double bilerp(double x, double y, double f00, double f01, double f10, double f11) {
		return f00 * (1.0 - x) * (1.0 - y) + f10 * x * (1.0 - y) + f01 * (1.0 - x) * y + f11 * x * y;
	}

	/**
	 * Bilinear interpolation of the value at x,y where 0<=x<=1 and 0<=y<=1
	 * given the values at (0,0), (0,1), (1,0) and (1,1). Note: This will work
	 * for other values of (x,y) but doesn't normally make sense.
	 * 
	 * @param x
	 *            the x position (in 0..1)
	 * @param y
	 *            the y position (in 0..1)
	 * @param f00
	 *            the value at (0,0)
	 * @param f01
	 *            the value at (0,1)
	 * @param f10
	 *            the value at (1,0)
	 * @param f11
	 *            the value at (1,1)
	 * @return the interpolated value (x,y)
	 */
	public static float bilerpf(float x, float y, float f00, float f01, float f10, float f11) {
		return f00 * (1.0f - x) * (1.0f - y) + f10 * x * (1.0f - y) + f01 * (1.0f - x) * y + f11 * x * y;
	}

	/**
	 * Bilinear interpolation of the value at x,y where 0<=x<=1 and 0<=y<=1
	 * given the values at (0,0), (0,1), (1,0) and (1,1). Note: This will work
	 * for other values of (x,y) but doesn't normally make sense.
	 * 
	 * @param x
	 *            the x position (in 0..1)
	 * @param y
	 *            the y position (in 0..1)
	 * @param f00
	 *            the value at (0,0)
	 * @param f01
	 *            the value at (0,1)
	 * @param f10
	 *            the value at (1,0)
	 * @param f11
	 *            the value at (1,1)
	 * @return the interpolated value (x,y)
	 */
	public static float bilerp(float x, float y, float f00, float f01, float f10, float f11) {
		return f00 * (1.0f - x) * (1.0f - y) + f10 * x * (1.0f - y) + f01 * (1.0f - x) * y + f11 * x * y;
	}

	/**
	 * Bicubic interpolation of the value at x,y where 0<=x<=1 and 0<=y<=1 given
	 * the values at integer coordinates from (-1,-1) to (3,3).
	 * 
	 * @param x
	 *            the x position (in 0..1)
	 * @param y
	 *            the y position (in 0..1)
	 * @param p
	 *            a 4x4 array of known values at (-1,-1) to (3,3)
	 * 
	 * @return the interpolated value (x,y)
	 */
	public static double bicubicInterp(double x, double y, double[][] p) {
		final double y0 = cubicInterp(y, p[0]);
		final double y1 = cubicInterp(y, p[1]);
		final double y2 = cubicInterp(y, p[2]);
		final double y3 = cubicInterp(y, p[3]);
		return cubicInterp(x, y0, y1, y2, y3);
	}

	/**
	 * Bicubic interpolation of the value at x,y where 0<=x<=1 and 0<=y<=1 given
	 * the values at integer coordinates from (-1,-1) to (3,3).
	 * 
	 * @param x
	 *            the x position (in 0..1)
	 * @param y
	 *            the y position (in 0..1)
	 * @param p
	 *            a 4x4 array of known values at (-1,-1) to (3,3)
	 * 
	 * @return the interpolated value (x,y)
	 */
	public static float bicubicInterp(float x, float y, float[][] p) {
		final float y0 = cubicInterp(y, p[0]);
		final float y1 = cubicInterp(y, p[1]);
		final float y2 = cubicInterp(y, p[2]);
		final float y3 = cubicInterp(y, p[3]);
		return cubicInterp(x, y0, y1, y2, y3);
	}
}
