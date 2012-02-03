package org.openimaj.demos.touchtable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Circle;

public class ReallyBasicTouchTracker {
	private long currentId = 0; 
	private List<Touch> lastPoints = new ArrayList<Touch>();
	private double threshold;
	
	public ReallyBasicTouchTracker(double threshold) {
		this.threshold = threshold;
	}
	
	public List<Touch> trackPoints(Collection<Touch> pts) {
		List<Touch> newPoints = new ArrayList<Touch>(pts.size());
		
		for (Touch pt : pts) {
			Touch matched = matchPoint(pt);
			
			if (matched == null) {
				newPoints.add(new Touch(pt, currentId++, null));
			} else {
				lastPoints.remove(matched);
				
				Point2dImpl mv = new Point2dImpl(pt.getCOG());
				mv.x -= matched.getX();
				mv.y -= matched.getY();
				
				newPoints.add(new Touch(pt, matched.touchID, mv));
			}
		}
		
		lastPoints = newPoints;
		
		return lastPoints;
	}

	private Touch matchPoint(Circle query) {
		double minDist = Double.MAX_VALUE;
		Touch best = null;
		
		for (Touch pt : lastPoints) {
			double dist = Line2d.distance(query.getCOG(), pt.getCOG());
			
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
