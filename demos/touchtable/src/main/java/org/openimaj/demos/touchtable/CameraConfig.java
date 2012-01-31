package org.openimaj.demos.touchtable;

import org.openimaj.io.ReadWriteableASCII;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.shape.Circle;

public interface CameraConfig extends ReadWriteableASCII{
	public Circle transformTouch(Circle point);
}
