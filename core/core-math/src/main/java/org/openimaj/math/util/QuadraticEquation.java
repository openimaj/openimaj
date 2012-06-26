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
 * Utility methods for dealing with quadratic equations.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class QuadraticEquation {
	/**
	 * Solve the general quadratic ax^2 + bx + c = 0 
	 * 
	 * @param a a
	 * @param b b
	 * @param c c
	 * @return the solution
	 */
	public static float[] solveGeneralQuadratic(float a, float b, float c) {
		float [] result = new float[2];

		float sqrt_discriminant = (float) Math.sqrt((b * b) - (4 * a * c));
		float r1 = ((-1 * b) + sqrt_discriminant) / (2 * a);
		float r2 = ((-1 * b) - sqrt_discriminant) / (2 * a);

		if (r1<r2) {
			result[0] = r1; result[1] = r2;
		} else {
			result[0] = r2; result[1] = r1;
		}

		return result;
	}

	/**
	 * Solve the general quadratic ax^2 + bx + c = 0
	 *  
	 * @param a a 
	 * @param b b
	 * @param c c
	 * @return the solution
	 */
	public static double[] solveGeneralQuadratic(double a, double b, double c) {
		double [] result = new double[2];

		double sqrt_discriminant = Math.sqrt((b * b) - (4 * a * c));
		double r1 = ((-1 * b) + sqrt_discriminant) / (2 * a);
		double r2 = ((-1 * b) - sqrt_discriminant) / (2 * a);

		if (r1<r2) {
			result[0] = r1; result[1] = r2;
		} else {
			result[0] = r2; result[1] = r1;
		}

		return result;
	}
}
