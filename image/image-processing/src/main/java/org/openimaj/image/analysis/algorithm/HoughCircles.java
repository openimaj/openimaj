package org.openimaj.image.analysis.algorithm;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.round;
import static java.lang.Math.sin;

import gnu.trove.map.hash.TIntFloatHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntFloatProcedure;
import gnu.trove.procedure.TIntObjectProcedure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.math.geometry.shape.Circle;


/**
 * An implementation of the Hough transform for circles.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class HoughCircles implements ImageAnalyser<FImage> {
	/**
	 * A circle with an associated weight.
	 * 
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	public static class WeightedCircle extends Circle {
		/**
		 * The weight 
		 */
		public float weight;
		
		/**
		 * Construct with the given geometry and weight.
		 * @param x the x-ordinate of the center
		 * @param y the y-ordinate of the center
		 * @param radius the radius of the circle
		 * @param weight the associated weight
		 */
		public WeightedCircle(float x, float y, float radius, float weight) {
			super(x, y, radius);
			this.weight = weight;
		}
	}
	
	protected int minRad;
	protected int maxRad;
	protected TIntObjectHashMap<TIntObjectHashMap<TIntFloatHashMap>> radmap;
	private float[][] cosanglemap;
	private float[][] sinanglemap;
	private float[][] radiusweight;
	private int nRadius;
	private int nDegree;
	private int radIncr;
	private int degIncr;

	/**
	 * Construct with the given parameters.
	 * 
	 * @param minRad minimum search radius
	 * @param maxRad maximum search radius
	 */
	public HoughCircles(int minRad, int maxRad) {
		super();
		this.minRad = minRad;
		this.maxRad = maxRad;
		this.radmap = new TIntObjectHashMap<TIntObjectHashMap<TIntFloatHashMap>>();
		this.radIncr = 5;
		this.degIncr = 5;
		this.nRadius = (maxRad-minRad) / this.radIncr;
		this.nDegree = 360 / this.degIncr;
		this.cosanglemap = new float[nRadius][nDegree];
		this.sinanglemap = new float[nRadius][nDegree];
		this.radiusweight = new float[nRadius][nDegree];
		for (int rad=minRad; rad<maxRad; rad+=this.radIncr) {
			for (int ang=0; ang<nDegree; ang+=this.degIncr) {
				double t = (ang * PI) / 180.0;
				this.cosanglemap [rad - minRad][ang] = (float) (rad*cos(t));
				this.sinanglemap [rad - minRad][ang] = (float) (rad*sin(t));
				this.radiusweight[rad - minRad][ang] = (float) (2 * Math.PI * rad);
			}
		}
	}

	@Override
	public void analyseImage(FImage image) {
		int height = image.getHeight();
		int width = image.getWidth();
		this.radmap = new TIntObjectHashMap<TIntObjectHashMap<TIntFloatHashMap>>();
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				if (image.pixels[y][x] == 1)
				{
					for (int rad = 0; rad < nRadius; rad+=this.radIncr) {
						for (int ang = 0; ang < nDegree; ang+=this.degIncr) {
							int x0 = round(x + this.cosanglemap[rad][ang]);
							int y0 = round(y + this.sinanglemap[rad][ang]);
	//						System.out.println(x0 + "," + y0 + " = " + this.radmap.pixels[y0+maxRad][x0+maxRad]);
							int actualrad = rad + this.minRad; 
							TIntObjectHashMap<TIntFloatHashMap> xMap = this.radmap.get(actualrad);
							if(xMap == null){
								xMap = new TIntObjectHashMap<TIntFloatHashMap>();
								this.radmap.put(actualrad, xMap);
							}
							TIntFloatHashMap yMap = xMap.get(x0);
							if(yMap == null){
								yMap = new TIntFloatHashMap();
								xMap.put(x0, yMap);
							}
							yMap.adjustOrPutValue(y0, 1f/this.radiusweight[rad][ang], 1f/this.radiusweight[rad][ang]);
//							yMap.adjustOrPutValue(y0, 1f, 1f);
							
						}
					}
				}
			}
		}
	}
	
	/**
	 * Get the n-best detected circles.
	 * @param n the number of circles to return
	 * @return the n best detected circles.
	 */
	public List<WeightedCircle> getBest(int n) {
		final List<WeightedCircle> toSort = new ArrayList<WeightedCircle>();
		this.radmap.forEachEntry(new TIntObjectProcedure<TIntObjectHashMap<TIntFloatHashMap>>() {
			
			@Override
			public boolean execute(final int radius, TIntObjectHashMap<TIntFloatHashMap> b) {
				b.forEachEntry(new TIntObjectProcedure<TIntFloatHashMap>() {

					@Override
					public boolean execute(final int x, TIntFloatHashMap b) {
						b.forEachEntry(new TIntFloatProcedure() {
							
							@Override
							public boolean execute(int y, float weightedCount) {
								toSort.add(new WeightedCircle(x,y,radius,weightedCount));
								return true;
							}
						});
						return true;
					}
				});
				return true;
			}
		});
		
		Collections.sort(toSort, new Comparator<WeightedCircle>(){

			@Override
			public int compare(WeightedCircle circ1, WeightedCircle circ2) {
				if(circ1.weight < circ2.weight)
					return 1;
				else if(circ1.weight > circ2.weight)
					return -1;
				else
					return 0;
			}
			
		});
		
		return toSort.subList(0, n > toSort.size() ? toSort.size() : n);
	}
}
