/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
		private final LinearTimeBasedFloatValueAnimator l;
		private final LinearTimeBasedFloatValueAnimator a;
		private final LinearTimeBasedFloatValueAnimator b;
		private final MBFImage buffer;

		ColourSpace space = ColourSpace.RGB;
		public LabColourAnimator(final Float[] start, final Float[] end, final long duration) {
			final MBFImage startImg = new MBFImage(1,1,3);
			startImg.setPixel(0, 0, start);
			final MBFImage endImg = new MBFImage(1,1,3);
			this.buffer = new MBFImage(1,1,3);
			endImg.setPixel(0, 0, end);
			final Float[] labstart = this.space.convertFromRGB(startImg).getPixel(0, 0);
			final Float[] labend = this.space.convertFromRGB(endImg).getPixel(0, 0);
			this.l = new LinearTimeBasedFloatValueAnimator(labstart[0], labend[0], duration);
			this.a = new LinearTimeBasedFloatValueAnimator(labstart[1], labend[1], duration);
			this.b = new LinearTimeBasedFloatValueAnimator(labstart[2], labend[2], duration);
		}
		@Override
		public Float[] nextValue() {
			this.buffer.setPixel(0, 0, new Float[]{this.l.nextValue(),this.a.nextValue(),this.b.nextValue()});
			final Float[] retPix = this.space.convertToRGB(this.buffer).getPixel(0, 0);
			return retPix;
		}

		@Override
		public boolean hasFinished() {
			return this.l.hasFinished();
		}

		@Override
		public void reset() {
			this.l.reset();
			this.a.reset();
			this.b.reset();
		}

	}
	private static final Float[] SEA_COLOUR = new Float[]{135f/255f,206f/255f,250f/255f};
	private static final Float[] LAND_COLOUR = new Float[]{238f/255f,232f/255f,170f/255f};
	private final int w;
	private final int h;
	private final WorldPolygons worldPolys;
	private final float scale;
	private final Map<String,LabColourAnimator> activeCountries = new HashMap<String, LabColourAnimator>();
	private final MBFImage img;

	/**
	 * @param w
	 * @param h
	 */
	public WorldVis(final int w, final int h) {
		super(new MBFImage(w,h,3));
		this.w = w;
		this.h = h;
		this.worldPolys = new WorldPolygons();
		this.scale = h/this.worldPolys.getBounds().height;
		this.img = new MBFImage(w, h, 3);
	}

	private MBFImage create(){
		this.img.fill(WorldVis.SEA_COLOUR);
		final Point2d mid = this.img.getBounds().calculateCentroid();
		Matrix trans = Matrix.identity(3, 3);
		trans = trans.times(
			TransformUtilities.scaleMatrixAboutPoint(
				this.scale, this.scale, mid
			)
		);
		trans = trans.times(
			TransformUtilities.rotationMatrixAboutPoint(Math.PI, mid.getX(), mid.getY())
		);
		trans = trans.times(
			TransformUtilities.translateMatrix(mid.getX(), mid.getY())
		);
		for (final WorldPlace wp : this.worldPolys.getShapes()) {

			final List<Shape> shapes = wp.getShapes();
			for (Shape s : shapes) {
				s = s.clone();

				s = s.transform(trans);
				this.img.drawShape(s,3, RGBColour.RED);
				if(this.activeCountries.containsKey(wp.getISOA2()))
				{
					this.img.drawShapeFilled(s, this.activeCountries.get(wp.getISOA2()).nextValue());
					if(this.activeCountries.get(wp.getISOA2()).hasFinished())
					{
						this.activeCountries.remove(wp.getISOA2());
					}
				}else{
					this.img.drawShapeFilled(s, WorldVis.LAND_COLOUR);
				}
			}
		}
		return this.img.flipX();
	}

	@Override
	protected void updateNextFrame(final MBFImage frame) {
		frame.internalAssign(this.create());

	}

	public void activate(final FreeGeoIPLocation geoip) {
		this.activeCountries.put(
			geoip.country_code.toLowerCase(),
			new LabColourAnimator(RGBColour.RED, WorldVis.LAND_COLOUR, 1000)
		);
	}

}
