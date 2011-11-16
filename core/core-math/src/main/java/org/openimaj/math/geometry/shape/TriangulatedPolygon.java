package org.openimaj.math.geometry.shape;

import java.util.List;

import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.triangulation.DelaunayTriangulator;

public class TriangulatedPolygon extends Polygon{
	private List<Triangle> triangles;

	public TriangulatedPolygon(Polygon p){
		super(p.isHole());
		this.vertices = p.vertices;
		triangles = DelaunayTriangulator.triangulate(p.vertices);
	}
	
	public TriangulatedPolygon(Shape transformedExpandedBounds) {
		this(transformedExpandedBounds.asPolygon());
	}

//	@Override
//	public boolean isInside(Point2d point){
//		for(Triangle t : triangles){
//			if(t.isInside(point)) return true;
//		}
//		return false;
//	}
}
