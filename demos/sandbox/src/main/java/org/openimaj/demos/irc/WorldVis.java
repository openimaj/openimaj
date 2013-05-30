package org.openimaj.demos.irc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.content.animation.animator.LinearTimeBasedFloatValueAnimator;
import org.openimaj.content.animation.animator.ValueAnimator;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.shape.Shape;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.text.geo.WorldPlace;
import org.openimaj.text.geo.WorldPolygons;
import org.openimaj.video.AnimatedVideo;

import Jama.Matrix;

public class WorldVis extends AnimatedVideo<MBFImage>{
	class LabColourAnimator implements ValueAnimator<Float[]>{
		private LinearTimeBasedFloatValueAnimator l;
		private LinearTimeBasedFloatValueAnimator a;
		private LinearTimeBasedFloatValueAnimator b;
		private MBFImage buffer;

		ColourSpace space = ColourSpace.RGB;
		public LabColourAnimator(Float[] start, Float[] end, long duration) {
			MBFImage startImg = new MBFImage(1,1,3);
			startImg.setPixel(0, 0, start);
			MBFImage endImg = new MBFImage(1,1,3);
			buffer = new MBFImage(1,1,3);
			endImg.setPixel(0, 0, end);
			Float[] labstart = space.convertFromRGB(startImg).getPixel(0, 0);
			Float[] labend = space.convertFromRGB(endImg).getPixel(0, 0);
			this.l = new LinearTimeBasedFloatValueAnimator(labstart[0], labend[0], duration);
			this.a = new LinearTimeBasedFloatValueAnimator(labstart[1], labend[1], duration);
			this.b = new LinearTimeBasedFloatValueAnimator(labstart[2], labend[2], duration);
		}
		@Override
		public Float[] nextValue() {
			buffer.setPixel(0, 0, new Float[]{l.nextValue(),a.nextValue(),b.nextValue()});
			Float[] retPix = space.convertToRGB(buffer).getPixel(0, 0);
			return retPix;
		}

		@Override
		public boolean hasFinished() {
			return l.hasFinished();
		}

		@Override
		public void reset() {
			l.reset();
			a.reset();
			b.reset();
		}

	}
	private static final Float[] SEA_COLOUR = new Float[]{135f/255f,206f/255f,250f/255f};
	private static final Float[] LAND_COLOUR = new Float[]{238f/255f,232f/255f,170f/255f};
	private int w;
	private int h;
	private WorldPolygons worldPolys;
	private float scale;
	private Map<String,LabColourAnimator> activeCountries = new HashMap<String, LabColourAnimator>();
	private MBFImage img;

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
		img = new MBFImage(w, h, 3);
	}

	private MBFImage create(){
		img.fill(SEA_COLOUR);
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
				img.drawShape(s,3, RGBColour.RED);
				if(this.activeCountries.containsKey(wp.getISOA2())){
					img.drawShapeFilled(s, this.activeCountries.get(wp.getISOA2()).nextValue());
					if(this.activeCountries.get(wp.getISOA2()).hasFinished()){
						this.activeCountries.remove(wp.getISOA2());
					}
				}else{
					img.drawShapeFilled(s, LAND_COLOUR);
				}
			}
		}
		return img.flipX();
	}

	@Override
	protected void updateNextFrame(MBFImage frame) {
		frame.internalAssign(this.create());

	}

	public void activate(FreeGeoIPLocation geoip) {
		this.activeCountries.put(
			geoip.country_code.toLowerCase(),
			new LabColourAnimator(RGBColour.RED, LAND_COLOUR, 10000)
		);
	}

}
