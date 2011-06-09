package org.openimaj.demos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Triangle;
import org.openimaj.math.geometry.triangulation.DelaunayTriangulator;

public class Triangulation {
	public static void main(String [] args) {
		Point2d[] pixels = {
				new Point2dImpl(0,0),
				new Point2dImpl(100,0),
				new Point2dImpl(20,20),
				new Point2dImpl(40,20),
				new Point2dImpl(60,20),
				new Point2dImpl(80,20),
				new Point2dImpl(45,55),
				new Point2dImpl(55,55),
				new Point2dImpl(50,60),
				new Point2dImpl(30,65),
				new Point2dImpl(70,65),
				new Point2dImpl(0,100),
				new Point2dImpl(100,100),
		};
		
		List<Triangle> tris = DelaunayTriangulator.triangulate(new ArrayList<Point2d>(Arrays.asList(pixels)));
		
		FImage image = new FImage(101, 101);
		
		for (Triangle t : tris) {
			image.drawShape(t, 1f);
		}
		System.out.println(tris);
		DisplayUtilities.display(image);
	}
}
