package org.openimaj.math.geometry.shape;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;

/**
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class TriangleTest {
	
	@Test
	public void testLineIntersection() throws Exception {
		Triangle t = new Triangle(
			new Point2dImpl(0,0), 
			new Point2dImpl(0.5f,1f),
			new Point2dImpl(1,0)
		);
		Point2d begin = new Point2dImpl(0f,0.5f);
		Point2d end = new Point2dImpl(1f,0.5f);
		
		Line2d midline = new Line2d(begin, end);
		
		Map<Line2d, Point2d> points = t.intersectionSides(midline);
		System.out.println(points);
		assertTrue(points.size() == 2);
		
		t = new Triangle(
			new Point2dImpl(0,0),
			new Point2dImpl(0.5f,1f),
			new Point2dImpl(0.5f,0f) 
		);
		points = t.intersectionSides(midline);
		System.out.println(points);
		assertTrue(points.size() == 2);
		
		t = new Triangle(
			new Point2dImpl(0,0.5f), 
			new Point2dImpl(0.5f,1f),
			new Point2dImpl(1,0.5f)
		);
		points = t.intersectionSides(midline);
		System.out.println(points);
	}
	@Test
	public void testIsInside(){
		Triangle t = new Triangle(
			new Point2dImpl(0,0),
			new Point2dImpl(0.5f,1f),
			new Point2dImpl(0.5f,0f) 
		);
		assertTrue(!t.isInside(new Point2dImpl(0.25f,0f)));
		assertTrue(t.isInsideOnLine(new Point2dImpl(0.25f,0f)));
		assertTrue(!t.isInside(new Point2dImpl(0.5f,0.5f)));
		assertTrue(t.isInsideOnLine(new Point2dImpl(0.5f,0.5f)));
		
	}

}
