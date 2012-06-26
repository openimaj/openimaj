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
package org.openimaj.math.geometry.shape;

import org.openimaj.math.geometry.point.Point2d;

/**
 * Methods for computing the area of intersection of two ellipses.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class EllipseAreaOfIntersection {

	class Polynomial2{
		
	}
	class Polynomial1{
		
	}
	
	class ComputedRoots{

		public int numDistinctRoots;
		public int[] multiplicity;
		
	}
	
	/**
	 * Compute area of intersection.
	 * @param E0 first ellipse.
	 * @param E1 second ellipse
	 * @return area of intersection.
	 */
	public static double AreaOfIntersection(Ellipse E0, Ellipse E1) {
		Polynomial2 Q0 = GetQuadraticRepresentation(E0); // Q0(x,y)
		Polynomial2 Q1 = GetQuadraticRepresentation(E1); // Q1(x,y)
		Polynomial1 B = GetBezoutDeterminant(Q0, Q1); // B(y)
		// Compute the roots of B. The input to ComputeRoots is B. The output numDistinctRoots is the number of distinct
		// real-valued roots. The output root[] stores the distinct roots, where only array elements 0 through
		// numDistinctRoots-1 are valid. The output multiplicity[] stores the number of times the roots occur. For a
		// distinct root, the multiplicity is 1. For a repeated root, the multiplicity is 2.
		
		ComputedRoots roots = ComputeRoots(B);
		// Compute the intersection points. The points are ordered counterclockwise about their centroid.
		Point2d[] intr = ComputeIntersections(E0, E1, Q0, Q1, roots);
		if (roots.numDistinctRoots == 0)
		{
			// Returns area(E0) [E0 is contained in E1], area(E1) [E1 is contained in E0], or zero [E0 and E1 are separated].
			return AreaOfIntersectionCS(E0, E1);
		}
		else if (roots.numDistinctRoots == 1) // multiplicity[0] must be 2.
		{
			return AreaOfIntersectionCS(E0, E1);
		}
		else if (roots.numDistinctRoots == 2)
		{
			if (roots.multiplicity[0] == 2) // Two roots, each repeated. multiplicity[1] must be 2.
			{
				return AreaOfIntersectionCS(E0, E1);
			}
			else // Two distinct roots. Region bounded by two arcs, one from each ellipse.
			{
				return AreaOfIntersection2(E0, E1, intr[0], intr[1]);
			}
		}
		else if (roots.numDistinctRoots == 3)
		{
			if (roots.multiplicity[0] == 2)
			{
				return AreaOfIntersection2(E0, E1, intr[1], intr[2]);
			}
			else if (roots.multiplicity[1] == 2)
			{
				return AreaOfIntersection2(E0, E1, intr[2], intr[0]);
			}
			else // multiplicity[2] == 2
			{
				return AreaOfIntersection2(E0, E1, intr[0], intr[1]);
			}
		}
		else // numDistinctRoots == 4
		{
			return AreaOfIntersection4(E0, E1, intr);
		}

	}
	private static Point2d[] ComputeIntersections(Ellipse e0, Ellipse e1, Polynomial2 q0, Polynomial2 q1, ComputedRoots roots) {
		// TODO Auto-generated method stub
		return null;
	}
	private static double AreaOfIntersection4(Ellipse e0, Ellipse e1, Point2d[] intr) {
		// TODO Auto-generated method stub
		return 0;
	}
	private static double AreaOfIntersection2(Ellipse e0, Ellipse e1, Point2d point2d, Point2d point2d2) {
		// TODO Auto-generated method stub
		return 0;
	}
	private static double AreaOfIntersectionCS(Ellipse e0, Ellipse e1) {
		// TODO Auto-generated method stub
		return 0;
	}
	private static ComputedRoots ComputeRoots(Polynomial1 b) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private static Polynomial1 GetBezoutDeterminant(Polynomial2 q0, Polynomial2 q1) {
		// TODO Auto-generated method stub
		return null;
	}
	private static Polynomial2 GetQuadraticRepresentation(Ellipse e0) {
		// TODO Auto-generated method stub
		return null;
	}

}
