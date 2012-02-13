package org.openimaj.math.geometry.shape;

import java.util.List;

import org.openimaj.math.geometry.triangulation.DelaunayTriangulator;

/**
 * A polygon that has been broken into triangles.
 * 
 * @author David Dupplaw <dpd@ecs.soton.ac.uk>
 */
public class TriangulatedPolygon extends Polygon {
	protected List<Triangle> triangles;

	/**
	 * Construct a {@link TriangulatedPolygon} with a polygon.
	 * Triangulation is performed with a {@link DelaunayTriangulator}.
	 * @param p The polygon.
	 */
	public TriangulatedPolygon(Polygon p) {
		super(p.isHole());
		this.vertices = p.vertices;
		triangles = DelaunayTriangulator.triangulate(p.vertices);
	}
	
	/**
	 * Construct a {@link TriangulatedPolygon} with a shape.
	 * Triangulation is performed with a {@link DelaunayTriangulator}.
	 * @param shape The shape.
	 */
	public TriangulatedPolygon(Shape shape) {
		this(shape.asPolygon());
	}

//	@Override
//	public boolean isInside(Point2d point){
//		for(Triangle t : triangles){
//			if(t.isInside(point)) return true;
//		}
//		return false;
//	}
}
