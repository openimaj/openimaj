package org.openimaj.image.feature.local.interest;

import org.openimaj.image.pixel.Pixel;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;

import Jama.Matrix;

public class EllipticKeyPoint {

	public float si;
	public Pixel centre;
	public float size;
	public Matrix transf;
	public Point2d axes;
	public double phi;

}
