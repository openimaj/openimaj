package org.openimaj.math.geometry.shape;

import gnu.trove.TIntArrayList;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;

public class PointListConnections {
	List<int[]> connections;

	public PointListConnections() {
		connections = new ArrayList<int[]>();
	}
	
	public PointListConnections(PointList pl, List<Line2d> lines) {
		this.connections = new ArrayList<int[]>();
		
		for (Line2d line : lines) {
			int i1 = pl.points.indexOf(line.begin);
			int i2 = pl.points.indexOf(line.end);
			
			connections.add( new int[] {i1, i2} );
		}
	}
	
	public void addConnection(int from, int to) {
		connections.add(new int [] {from, to});
	}
	
	public List<Point2d> getConnections(Point2d pt, PointList pl) {
		return null;
	}
	
	public int[] getConnections(int id) {
		TIntArrayList conns = new TIntArrayList();
		
		for (int[] c : connections) {
			if (c[0] == id)
				conns.add(c[1]);
			if (c[1] == id)
				conns.add(c[0]);
		}
		
		return conns.toNativeArray();
	}
	
	public Point2d calculateNormal(Point2d pt, PointList pointList) {
		return calculateNormal(pointList.points.indexOf(pt), pointList);
	}
	
	public Point2d calculateNormal(int id, PointList pointList) {
		int[] conns = getConnections(id);
		
		if (conns.length == 1) {
			Point2d p0 = pointList.points.get(id);
			Point2d p1 = pointList.points.get(conns[0]);
			
			Line2d line = new Line2d(p0, p1);
			Line2d normal = line.getNormal();
			
			return normal.toUnitVector(); 
		} else if (conns.length == 2) {
			return null;
		} else {
			return null;
		}
	}
	
	public List<Line2d> getLines(PointList pointList) {
		List<Line2d> lines = new ArrayList<Line2d>(connections.size());
		
		for (int[] conn : connections) {
			lines.add(new Line2d(
				pointList.points.get(conn[0]), 
				pointList.points.get(conn[1])
			));
		}
		
		return lines;
	}
}
