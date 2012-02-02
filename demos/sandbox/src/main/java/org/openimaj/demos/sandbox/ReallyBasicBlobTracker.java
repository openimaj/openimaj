package org.openimaj.demos.sandbox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;

public class ReallyBasicBlobTracker<T extends Point2d> {
	public class LabelledPoint {
		public long id;
		public T point;
		
		public LabelledPoint(T pt, long id) {
			this.point = pt;
			this.id = id;
		}
	}

	private long currentId = 0; 
	private List<LabelledPoint> lastPoints = new ArrayList<LabelledPoint>();
	private double threshold;
	
	public ReallyBasicBlobTracker(double threshold) {
		this.threshold = threshold;
	}
	
	public List<LabelledPoint> trackPoints(Collection<T> pts) {
		List<LabelledPoint> newPoints = new ArrayList<LabelledPoint>(pts.size());
		
		for (T pt : pts) {
			LabelledPoint matched = matchPoint(pt);
			
			if (matched == null) {
				newPoints.add(new LabelledPoint(pt, currentId++));
			} else {
				lastPoints.remove(matched);
				newPoints.add(new LabelledPoint(pt, matched.id));
			}
		}
		
		lastPoints = newPoints;
		
		return lastPoints;
	}

	private LabelledPoint matchPoint(Point2d query) {
		double minDist = Double.MAX_VALUE;
		LabelledPoint best = null;
		
		for (LabelledPoint pt : lastPoints) {
			double dist = Line2d.distance(query, pt.point);
			
			if (dist < minDist) {
				minDist = dist;
				best = pt;
			}
		}
		
		if (minDist > threshold) 
			return null;
		
		return best;
	}
}
