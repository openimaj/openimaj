package org.openimaj.image.processing.algorithm;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.round;
import static java.lang.Math.sin;

import gnu.trove.TIntFloatHashMap;
import gnu.trove.TIntFloatProcedure;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntObjectProcedure;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processor.ImageProcessor;
import org.openimaj.math.geometry.shape.Circle;


public class HoughCircles implements ImageProcessor<FImage> {
	protected int minRad;
	protected int maxRad;
	public TIntObjectHashMap<TIntObjectHashMap<TIntFloatHashMap>> radmap;
	private float[][] cosanglemap;
	private float[][] sinanglemap;
	private float[][] radiusweight;
	private int nRadius;
	private int nDegree;
	private int radIncr;
	private int degIncr;

	public HoughCircles(int minRad, int maxRad, int width, int height) {
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
	public void processImage(FImage image) {
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
	public static class WeightedCircle extends Circle{
		public WeightedCircle(float x, float y, float radius, float weight) {
			super(x, y, radius);
			this.weight = weight;
		}

		public float weight;
		
	}
	public List<WeightedCircle> getBest(int n){
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
