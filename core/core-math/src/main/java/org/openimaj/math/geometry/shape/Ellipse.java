package org.openimaj.math.geometry.shape;

import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.math.matrix.MatrixUtils;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.util.pair.IndependentPair;

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
		return this.asPolygon().transform(transform);
	}
	
	public Ellipse transformAffine(Matrix transform) {
		Point2d newCOG = this.getCOG().transform(transform);
		Matrix translated = transform.times(TransformUtilities.translateMatrix((float)this.x, (float)this.y));
		Matrix affineTransform = TransformUtilities.homographyToAffine(translated);
		
		Matrix newTransform = this.transformMatrix().times(affineTransform);
		
		Point2dImpl zero = new Point2dImpl(0,0);
		Point2dImpl majorpoint = new Point2dImpl((float)newTransform.get(0, 0),(float)newTransform.get(1, 0));
		Point2dImpl minorpoint = new Point2dImpl((float)newTransform.get(1, 0),(float)newTransform.get(1, 1));
		double newMajor = new Line2d(zero,majorpoint).calculateLength();
		double newMinor = new Line2d(zero,minorpoint).calculateLength();
		return new Ellipse(newCOG.getX(),newCOG.getY(),newMajor, newMinor,newRotation);
		
	}
	
	/**
	 * Normalised transform matrix such that the scale of this ellipse is removed (i.e. the semi-major axis is 1)
	 * @return
	 */
	public Matrix normTransformMatrix() {
		double cosrot = Math.cos(rotation);
		double sinrot = Math.sin(rotation);
//		
//		double scaledMajor = 1.0;
//		double scaledMinor = minor / major;
//		
//		double xMajor =  cosrot * scaledMajor;
//		double yMajor =  sinrot * scaledMajor;
//		double xMinor = -sinrot * scaledMinor;
//		double yMinor =  cosrot * scaledMinor;
//		return new Matrix(new double[][]{
//			{xMajor,xMinor,this.x},	
//			{yMajor,yMinor,this.y},
//			{0,0,1}
//		});
		double cosrotsq = cosrot * cosrot;
		double sinrotsq = sinrot * sinrot;
		
		double scale = Math.sqrt(major * minor);
		
		double majorsq = (major * major) / (scale * scale);
		double minorsq = (minor * minor) / (scale * scale);
		double Cxx = (cosrotsq / majorsq) + (sinrotsq/minorsq);
		double Cyy = (sinrotsq / majorsq) + (cosrotsq/minorsq);
		double Cxy = sinrot * cosrot * ((1/majorsq) - (1/minorsq));
		double detC = Cxx*Cyy - (Cxy*Cxy);
		
		Matrix cMat = new Matrix(new double[][]{
			{Cyy/detC,-Cxy/detC},
			{-Cxy/detC,Cxx/detC}
		});
		
		cMat = MatrixUtils.sqrt(cMat);
//		cMat = cMat.inverse();
		Matrix retMat = new Matrix(new double[][]{
				{cMat.get(0,0),cMat.get(0,1),this.x},
				{cMat.get(1,0),cMat.get(1,1),this.y},
				{0,0,1},
		});
		return retMat;
	}
	
	/**
	 * The transform matrix required to turn points on a unit circle into the points on this ellipse.
	 * This function is used by {@link Ellipse#asPolygon} 
	 * @return 
	 */
	public Matrix transformMatrix(){
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
		
//		double cosrotsq = cosrot * cosrot;
//		double sinrotsq = sinrot * sinrot;
//		
//		double scale = Math.sqrt(major * minor);
//		
//		double majorsq = (major * major) / (scale * scale);
//		double minorsq = (minor * minor) / (scale * scale);
//		double Cxx = (cosrotsq / majorsq) + (sinrotsq/minorsq);
//		double Cyy = (sinrotsq / majorsq) + (cosrotsq/minorsq);
//		double Cxy = sinrot * cosrot * ((1/majorsq) - (1/minorsq));
//		double detC = Cxx*Cyy - (Cxy*Cxy);
//		
//		Matrix cMat = new Matrix(new double[][]{
//			{Cxx/detC,-Cxy/detC},
//			{-Cxy/detC,Cyy/detC}
//		});
//		
//		cMat = cMat.inverse();
//		Matrix retMat = new Matrix(new double[][]{
//				{cMat.get(0,0),cMat.get(0,1),this.x},
//				{cMat.get(1,0),cMat.get(1,1),this.y},
//				{0,0,1},
//		});
//		return retMat;
		
	}

	@Override
	public Polygon asPolygon() {
		Polygon e = new Polygon();
		
		Matrix transformMatrix = this.transformMatrix();
		Point2dImpl circlePoint = new Point2dImpl(0,0);
		for(double t = -Math.PI; t < Math.PI ; t+=Math.PI/360){
			circlePoint.x = (float) Math.cos(t);
			circlePoint.y = (float) Math.sin(t);
			e.vertices.add(circlePoint.transform(transformMatrix));
		}
		return e;
	}
	
	@Override
	public double intersectionArea(Shape that) {
		return intersectionArea(that,1);
	}

	@Override
	public double intersectionArea(Shape that, int nStepsPerDimention) {
		Rectangle overlapping = this.calculateRegularBoundingBox().overlapping(that.calculateRegularBoundingBox());
		if(overlapping==null)
			return 0;
		double intersection = 0;
		double step = Math.max(overlapping.width, overlapping.height)/(double)nStepsPerDimention;
		double nReads = 0;
		for(float x = overlapping.x; x < overlapping.x + overlapping.width; x+=step){
			for(float y = overlapping.y; y < overlapping.y + overlapping.height; y+=step){
				boolean insideThis = this.isInside(new Point2dImpl(x,y));
				boolean insideThat = that.isInside(new Point2dImpl(x,y));
				nReads++;
				if(insideThis && insideThat) {
					intersection++;
				}
			}
		}
		
		return (intersection/nReads) * (overlapping.width * overlapping.height);
	}



	public double getMinor() {
		return this.minor;
	}



	public double getMajor() {
		return this.major;
	}



	public IndependentPair<Matrix, Double> secondMomentsAndScale() {
		return null;
	}
	
	@Override
	public boolean equals(Object other){
		if(!(other instanceof Ellipse)) return false;
		Ellipse that = (Ellipse)other;
		return this.major == that.major && this.minor == that.minor && this.x == that.x && this.y == that.y && this.rotation == that.rotation;
	}
}
