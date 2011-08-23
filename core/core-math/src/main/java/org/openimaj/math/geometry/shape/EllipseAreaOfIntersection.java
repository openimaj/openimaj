package org.openimaj.math.geometry.shape;

import org.openimaj.math.geometry.point.Point2d;

public class EllipseAreaOfIntersection {

	class Polynomial2{
		
	}
	class Polynomial1{
		
	}
	
	class ComputedRoots{

		public int numDistinctRoots;
		public int[] multiplicity;
		
	}
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
