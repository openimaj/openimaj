package org.openimaj.math.geometry.shape;

import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.util.array.ArrayUtils;

import Jama.Matrix;

public class Ellipse implements Shape{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7665274980689001203L;
	private double x;
	private double y;
	private double major;
	private double minor;
	private double rotation;
	
	public Ellipse(double x, double y, double major, double minor, double rotation){
		this.x = x;
		this.y = y;
		this.major = major;
		this.minor = minor;
		this.rotation = rotation;
	}
	
	
	
	@Override
	public boolean isInside(Point2d point) {
		// Unrotate the point relative to the center of the ellipse
		double cosrot = Math.cos(-rotation);
		double sinrot = Math.sin(-rotation);
		double relx = (point.getX() - x);
		double rely = (point.getY() - y);
		
		double xt =  cosrot * relx - sinrot * rely;
		double yt =  sinrot * relx + cosrot * rely;
		
		double ratiox = xt / major ;
		double ratioy = yt / minor ;
		
		return ratiox * ratiox + ratioy * ratioy <=1;
	}

	@Override
	public Rectangle calculateRegularBoundingBox() {
		
		// Differentiate the parametrics form of the ellipse equation to get
		// tan(t) = -semiMinor * tan(rotation) / semiMajor  (which gives us the min/max X)
		// tan(t) = semiMinor * cot(rotation) / semiMajor (which gives us the min/max Y)
		// 
		// We find a value for t, add PI to get another value of t, we use this to find our min/max x/y
		
		double[] minmaxx = new double[2];
		double[] minmaxy = new double[2];
		double tanrot = Math.tan(rotation);
		double cosrot = Math.cos(rotation);
		double sinrot = Math.sin(rotation);
		
		double tx = Math.atan(-minor * tanrot / major);
		double ty = Math.atan(minor * (1 / tanrot) / major);
		
		minmaxx[0] = x + (major * Math.cos(tx) * cosrot - minor * Math.sin(tx) * sinrot);tx+=Math.PI;
		minmaxx[1] = x + (major * Math.cos(tx) * cosrot - minor * Math.sin(tx) * sinrot);
		minmaxy[0] = y + (major * Math.cos(ty) * sinrot + minor * Math.sin(ty) * cosrot);ty+=Math.PI;
		minmaxy[1] = y + (major * Math.cos(ty) * sinrot + minor * Math.sin(ty) * cosrot);
		
		double minx,miny,maxx,maxy;
		minx = minmaxx[ArrayUtils.minIndex(minmaxx)];
		miny = minmaxy[ArrayUtils.minIndex(minmaxy)];
		maxx = minmaxx[ArrayUtils.maxIndex(minmaxx)];
		maxy = minmaxy[ArrayUtils.maxIndex(minmaxy)];
		
		return new Rectangle((float)minx,(float)miny,(float)(maxx-minx),(float)(maxy-miny));
	}
	
	public Polygon calculateOrientedBoundingBox() {
		
		double minx = (-major);
		double miny = (-minor);
		double maxx = (+major);
		double maxy = (+minor);
		
		Matrix corners = new Matrix(new double[][]{
				{minx,miny,1},
				{minx,maxy,1},
				{maxx,miny,1},
				{maxx,maxy,1},
		});
		corners = corners.transpose();
		Matrix rot = TransformUtilities.rotationMatrix(rotation);
		Matrix rotated = rot.times(corners);
		double[][] rotatedData = rotated.getArray();
		double[] rx = ArrayUtils.add(rotatedData[0],(double)this.x);
		double[] ry = ArrayUtils.add(rotatedData[1],(double)this.y);
		Polygon ret = new Polygon();
		ret.vertices.add(new Point2dImpl((float)rx[0],(float)ry[0]));
		ret.vertices.add(new Point2dImpl((float)rx[2],(float)ry[2]));
		ret.vertices.add(new Point2dImpl((float)rx[3],(float)ry[3]));
		ret.vertices.add(new Point2dImpl((float)rx[1],(float)ry[1]));
		return ret;
	}

	@Override
	public void translate(float x, float y) {
		this.x+=x;
		this.y+=y;
	}

	@Override
	public void scale(float sc) {
		this.x*=sc;
		this.y*=sc;
		this.major*=sc;
		this.minor*=sc;
	}

	@Override
	public void scale(Point2d centre, float sc) {
		this.translate( -centre.getX(), -centre.getY() );
		scale(sc);
		this.translate( centre.getX(), centre.getY() );
	}

	@Override
	public void scaleCOG(float sc) {	
		this.major*=sc;
		this.minor*=sc;
	}

	@Override
	public Point2d getCOG() {
		return new Point2dImpl((float)x,(float)y);
	}

	@Override
	public double calculateArea() {
		return Math.PI * major * minor;
	}

	@Override
	public double minX() {
		return this.calculateRegularBoundingBox().minX();
	}

	@Override
	public double minY() {
		return this.calculateRegularBoundingBox().minY();
	}

	@Override
	public double maxX() {
		return this.calculateRegularBoundingBox().maxX();
	}

	@Override
	public double maxY() {
		return this.calculateRegularBoundingBox().maxY();
	}

	@Override
	public double getWidth() {
		return this.calculateRegularBoundingBox().getWidth();
	}

	@Override
	public double getHeight() {
		return this.calculateRegularBoundingBox().getHeight();
	}

	@Override
	public Shape transform(Matrix transform) {
		Matrix newEllipseMat = transform.times(this.asTransforomMatrix());
		double majx = newEllipseMat.get(0, 0);
		double majy = newEllipseMat.get(1, 0);
		double minx = newEllipseMat.get(0, 1);
		double miny = newEllipseMat.get(1, 1);
		
		double newEllipseX = newEllipseMat.get(0, 2);
		double newEllipseY = newEllipseMat.get(1, 2);
		double newEllipseRotation = Math.atan2(majy,majx);
		double newEllipseMajor = Math.sqrt(majy * majy + majx * majx);
		double newEllipseMinor = Math.sqrt(miny * miny + minx * minx);
		return new Ellipse(newEllipseX,newEllipseY,newEllipseMajor,newEllipseMinor,newEllipseRotation);
	}
	
	public Matrix asTransforomMatrix(){
		double cosrot = Math.cos(rotation);
		double sinrot = Math.sin(rotation);
		
		double xMajor =  cosrot * major;
		double yMajor =  sinrot * major;
		double xMinor = -sinrot * minor;
		double yMinor =  cosrot * minor;
		return new Matrix(new double[][]{
			{xMajor,xMinor,this.x},	
			{yMajor,yMinor,this.y},
			{0,0,1}
		});
	}

	@Override
	public Polygon asPolygon() {
		Polygon e = new Polygon();
		
		Matrix transformMatrix = this.asTransforomMatrix();
		Point2dImpl circlePoint = new Point2dImpl(0,0);
		for(double t = -Math.PI; t < Math.PI ; t+=Math.PI/360){
			circlePoint.x = (float) Math.cos(t);
			circlePoint.y = (float) Math.sin(t);
			e.vertices.add(circlePoint.transform(transformMatrix));
		}
		return e;
	}

}
