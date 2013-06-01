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
package org.openimaj.image.analysis.algorithm;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.round;
import static java.lang.Math.sin;
import gnu.trove.map.hash.TIntFloatHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntFloatProcedure;
import gnu.trove.procedure.TIntObjectProcedure;

import java.util.List;

import org.apache.log4j.Logger;
import org.openimaj.image.FImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.math.geometry.shape.Circle;
import org.openimaj.util.queue.BoundedPriorityQueue;

/**
 * An implementation of the Hough transform for circles.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class HoughCircles implements ImageAnalyser<FImage> {
	Logger logger = Logger.getLogger(HoughCircles.class);

	/**
	 * A circle with an associated weight.
	 * 
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	public static class WeightedCircle extends Circle implements Comparable<WeightedCircle> {
		/**
		 * The weight
		 */
		public float weight;

		/**
		 * Construct with the given geometry and weight.
		 * 
		 * @param x
		 *            the x-ordinate of the center
		 * @param y
		 *            the y-ordinate of the center
		 * @param radius
		 *            the radius of the circle
		 * @param weight
		 *            the associated weight
		 */
		public WeightedCircle(float x, float y, float radius, float weight) {
			super(x, y, radius);
			this.weight = weight;
		}

		@Override
		public int compareTo(WeightedCircle o) {
			return Float.compare(o.weight, this.weight);
		}
	}

	protected int minRad;
	protected int maxRad;
	protected TIntObjectHashMap<TIntObjectHashMap<TIntFloatHashMap>> radmap;
	private float[][] cosanglemap;
	private float[][] sinanglemap;
	private int nRadius;
	private int nDegree;
	private int radIncr;

	/**
	 * Construct with the given parameters.
	 * 
	 * @param minRad
	 *            minimum search radius
	 * @param maxRad
	 *            maximum search radius
	 * @param radIncrement
	 *            amount to increment search radius by between min and max.
	 * @param nDegree
	 *            number of degree increments
	 */
	public HoughCircles(int minRad, int maxRad, int radIncrement, int nDegree) {
		super();
		this.minRad = minRad;
		if (this.minRad <= 0)
			this.minRad = 1;
		this.maxRad = maxRad;
		this.radmap = new TIntObjectHashMap<TIntObjectHashMap<TIntFloatHashMap>>();
		this.radIncr = radIncrement;
		this.nRadius = (maxRad - minRad) / this.radIncr;
		this.nDegree = nDegree;
		this.cosanglemap = new float[nRadius][nDegree];
		this.sinanglemap = new float[nRadius][nDegree];
		for (int radIndex = 0; radIndex < this.nRadius; radIndex++) {
			for (int angIndex = 0; angIndex < nDegree; angIndex++) {
				final double ang = angIndex * (2 * PI / nDegree);
				final double rad = minRad + (radIndex * this.radIncr);
				this.cosanglemap[radIndex][angIndex] = (float) (rad * cos(ang));
				this.sinanglemap[radIndex][angIndex] = (float) (rad * sin(ang));
			}
		}
	}

	@Override
	public void analyseImage(FImage image) {
		final int height = image.getHeight();
		final int width = image.getWidth();
		this.radmap = new TIntObjectHashMap<TIntObjectHashMap<TIntFloatHashMap>>();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (image.pixels[y][x] == 1)
				{
					for (int rad = 0; rad < nRadius; rad++) {
						final int actualrad = (rad * this.radIncr) + this.minRad;
						final float radiusWeight = 1f / this.nDegree;
						// if(actualrad == 0){
						// throw new
						// RuntimeException("The weight should never be 0");
						// }
						for (int ang = 0; ang < nDegree; ang++) {
							final int x0 = round(x + this.cosanglemap[rad][ang]);
							final int y0 = round(y + this.sinanglemap[rad][ang]);

							TIntObjectHashMap<TIntFloatHashMap> xMap = this.radmap.get(actualrad);
							if (xMap == null) {
								this.radmap.put(actualrad, xMap = new TIntObjectHashMap<TIntFloatHashMap>());
							}
							TIntFloatHashMap yMap = xMap.get(x0);
							if (yMap == null) {
								xMap.put(x0, yMap = new TIntFloatHashMap());
							}
							yMap.adjustOrPutValue(y0, radiusWeight, radiusWeight);
							// if(x0 == 37 && y0 == 22 && actualrad == 1){
							// logger.debug("This should not be !");
							// logger.debug(String.format("Pixel = %d,%d",
							// x,y));
							// logger.debug(String.format("x=%d,y=%d,r=%d,v=%2.5f",x0
							// ,y0 ,actualrad , newValue ));
							// }
							// if(x0 > 22 && x0 < 27 && y0 > 22 && y0 < 27 &&
							// actualrad > 10 && actualrad < 14){
							// logger.debug("This should be correct!");
							// logger.debug(String.format("x=%d,y=%d,r=%d,v=%2.5f",x0
							// ,y0 ,actualrad , newValue ));
							// }
							// if(Float.isInfinite(newValue)){
							// throw new
							// RuntimeException("The value held should never be infinity");
							// }
							// logger.debug(String.format("x=%d,y=%d,r=%d,v=%2.5f\n",x0
							// ,y0 ,actualrad , newValue ));
							// maxWeight = Math.max(newValue, maxWeight);
						}
					}
				}
			}
		}
		logger.debug("Done analysing the image!");
	}

	/**
	 * Get the n-best detected circles.
	 * 
	 * @param n
	 *            the number of circles to return
	 * @return the n best detected circles.
	 */
	public List<WeightedCircle> getBest(int n) {
		// final List<WeightedCircle> toSort = new ArrayList<WeightedCircle>();
		final BoundedPriorityQueue<WeightedCircle> bpq = new BoundedPriorityQueue<WeightedCircle>(n);
		this.radmap.forEachEntry(new TIntObjectProcedure<TIntObjectHashMap<TIntFloatHashMap>>() {

			@Override
			public boolean execute(final int radius, TIntObjectHashMap<TIntFloatHashMap> b) {
				b.forEachEntry(new TIntObjectProcedure<TIntFloatHashMap>() {

					@Override
					public boolean execute(final int x, TIntFloatHashMap b) {
						b.forEachEntry(new TIntFloatProcedure() {

							@Override
							public boolean execute(int y, float weightedCount) {
								bpq.offer(new WeightedCircle(x, y, radius, weightedCount));
								return true;
							}
						});
						return true;
					}
				});
				return true;
			}
		});

		return bpq.toOrderedList();
	}
}
