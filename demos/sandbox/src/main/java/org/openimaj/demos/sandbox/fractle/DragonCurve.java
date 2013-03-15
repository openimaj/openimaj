package org.openimaj.demos.sandbox.fractle;

import java.util.Collections;
import java.util.Iterator;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.PointList;
import org.openimaj.math.geometry.transforms.TransformUtilities;

import Jama.Matrix;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class DragonCurve {

	private PointList currentCurve;
	private Point2d startOfDragon;
	private Point2d endOfDragon;
	public DragonCurve() {
		this.startOfDragon = new Point2dImpl(1/3f,0.5f);
		endOfDragon = new Point2dImpl(2/3f,0.5f);
		this.currentCurve = new PointList(startOfDragon,endOfDragon);
	}

	public PointList nextHalfCurve() {
		Matrix dragonTurn = TransformUtilities.rotationMatrixAboutPoint(-Math.PI/2f, startOfDragon.getX(), startOfDragon.getY());
		PointList newCurve = this.currentCurve.transform(dragonTurn);
		return newCurve;
	}

	public PointList nextCurve(float prop) {
		Matrix dragonTurn = TransformUtilities.rotationMatrixAboutPoint((-Math.PI/2f)*prop, startOfDragon.getX(), startOfDragon.getY());
		PointList newCurve = this.currentCurve.transform(dragonTurn);
		return newCurve;
	}

	public void iterate() {
		PointList newCurve = nextHalfCurve();
		Collections.reverse(newCurve.points);
		for (Point2d point2d : this.currentCurve) {
			if(!point2d.equals(this.startOfDragon))
				newCurve.points.add(point2d);
		}
		Point2dImpl minXY = minXY(newCurve);
		Point2dImpl maxXY = maxXY(newCurve);
		double bbScalar = Math.max(maxXY.x - minXY.x,maxXY.y - minXY.y);
		Matrix translateToPointMatrix = TransformUtilities.translateToPointMatrix(
				minXY,
				new Point2dImpl(1/3f,1/3f)
		);
		Matrix translate = translateToPointMatrix;
		float d = (float) ((1/3f)/bbScalar);
		Matrix scale = TransformUtilities.scaleMatrix(d, d);
		Matrix transform = translate.times(scale);
//		Matrix transform = translate;
		newCurve = newCurve.transform(transform);
		this.currentCurve = newCurve;
		this.startOfDragon = newCurve.points.get(0);
		this.endOfDragon = newCurve.points.get(newCurve.points.size()-1);
	}

	interface StepListener{
		public void state(PointList list);
	}
	public void iterate(StepListener listener, int nSteps) {

		PointList newCurve = null;

		for (int i = 0; i <= nSteps; i++) {
			newCurve = nextCurve((float)i/nSteps);
			Collections.reverse(newCurve.points);
			for (Point2d point2d : this.currentCurve) {
				if(!point2d.equals(this.startOfDragon))
					newCurve.points.add(point2d);
			}
			prune(newCurve);
			Point2dImpl minXY = minXY(newCurve);
			Point2dImpl maxXY = maxXY(newCurve);
			double bbScalar = Math.max(maxXY.x - minXY.x,maxXY.y - minXY.y);
			Matrix translateToPointMatrix = TransformUtilities.translateToPointMatrix(
					minXY,
					new Point2dImpl(1/3f,1/3f)
			);
			Matrix translate = translateToPointMatrix;
			float d = (float) ((1/3f)/bbScalar);
			Matrix scale = TransformUtilities.scaleMatrix(d, d);
			Matrix transform = translate.times(scale);
	//		Matrix transform = translate;
			newCurve = newCurve.transform(transform);
			listener.state(newCurve);
		}
		this.currentCurve = newCurve;
		this.startOfDragon = newCurve.points.get(0);
		this.endOfDragon = newCurve.points.get(newCurve.points.size()-1);
	}


	private void prune(PointList newCurve) {
		Point2d prev = null;
		for (Iterator<Point2d> iterator = newCurve.iterator(); iterator.hasNext();) {
			Point2d p = iterator.next();
			if(prev!= null){
				if(Line2d.distance(prev, p) < 1/200f){
					iterator.remove();
				}
				else{
					prev = p;
				}
			}else{
				prev = p;
			}
		}
	}

	private Point2dImpl minXY(PointList newCurve) {
		float minX = Float.MAX_VALUE;
		float minY = Float.MAX_VALUE;
		for (Point2d point2d : newCurve) {
			float px = point2d.getX();
			float py = point2d.getY();
			minX = Math.min(px, minX);
			minY = Math.min(py, minY);
		}
		return new Point2dImpl(minX,minY);
	}

	private Point2dImpl maxXY(PointList newCurve) {
		float maxX = Float.MIN_VALUE;
		float maxY = Float.MIN_VALUE;
		for (Point2d point2d : newCurve) {
			float px = point2d.getX();
			float py = point2d.getY();
			maxX = Math.max(px, maxX);
			maxY = Math.max(py, maxY);
		}
		return new Point2dImpl(maxX,maxY);
	}


	public static void main(String[] args) throws InterruptedException {
		final MBFImage img = new MBFImage(800,800,3);
		final Matrix scaleMatrix = TransformUtilities.scaleMatrix(img.getWidth(), img.getHeight());
		DragonCurve curve = new DragonCurve();
		while(true){

			curve.iterate(new StepListener() {

				@Override
				public void state(PointList curve) {
					img.fill(RGBColour.BLACK);
					Point2d prev = null;
					for (Point2d p : curve) {
						p = p.transform(scaleMatrix);
						if(prev!=null){
							img.drawLine(p, prev, 1,RGBColour.RED);
						}
						prev = p;
					}
					DisplayUtilities.displayName(img,"dragon");
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			},100);

		}
	}

	private PointList getScaledCurve(int width, int height) {
		return this.currentCurve.clone().scaleXY(width, height);
	}
}
