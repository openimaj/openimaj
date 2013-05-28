package org.openimaj.demos.irc;

import java.util.List;

import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Shape;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.text.geo.WorldPlace;
import org.openimaj.text.geo.WorldPolygons;
import org.openimaj.video.AnimatedVideo;

import Jama.Matrix;

public class WorldVis extends AnimatedVideo<MBFImage>{
	private int w;
	private int h;
	private WorldPolygons worldPolys;
	private float scale;

	/**
	 * @param w
	 * @param h
	 */
	public WorldVis(int w, int h) {
		super(new MBFImage(w,h,3));
		this.w = w;
		this.h = h;
		this.worldPolys = new WorldPolygons();
		this.scale = h/this.worldPolys.getBounds().height;
	}
	
	private MBFImage create(){
		MBFImage img = new MBFImage(w, h, 3);
		Point2d mid = img.getBounds().getCOG();
		Matrix trans = Matrix.identity(3, 3);
		trans = trans.times(
			TransformUtilities.scaleMatrixAboutPoint(
				scale, scale, mid
			)
		);
		trans = trans.times(
			TransformUtilities.rotationMatrixAboutPoint(Math.PI, mid.getX(), mid.getY())
		);
		trans = trans.times(
			TransformUtilities.translateMatrix(mid.getX(), mid.getY())
		);
		for (WorldPlace wp : worldPolys.getShapes()) {
			
			List<Shape> shapes = wp.getShapes();
			for (Shape s : shapes) {
				s = s.clone();
				
				s = s.transform(trans);
				img.drawShape(s, RGBColour.RED);
			} 
			
			
		}
		return img.flipX();
	}

	@Override
	protected void updateNextFrame(MBFImage frame) {
		frame.internalAssign(this.create());
		
	}
	
}
