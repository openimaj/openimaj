package org.openimaj.demos.touchtable;

import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Circle;

public class Touch extends Circle {
	public long touchID;
	public Point2dImpl motionVector;

	public Touch(Circle c, long touchID, Point2dImpl motionVector) {
		super(c);
		
		this.touchID = touchID;
		this.motionVector = motionVector;
	}

	public Touch(float x, float y, float rad) {
		super(x, y, rad);
	}

	public Touch(Circle circle) {
		super(circle);
	}

	public Touch(float x, float y, float radius, long touchID, Point2dImpl motionVector) {
		super(x, y, radius);
		
		this.touchID = touchID;
		this.motionVector = motionVector;
	}
}
