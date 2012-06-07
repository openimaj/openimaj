package org.openimaj.demos.sandbox.tldcpp;

import org.apache.commons.math.analysis.interpolation.BicubicSplineInterpolator;
import org.openimaj.image.FImage;
import org.openimaj.image.processing.algorithm.MeanCenter;
import org.openimaj.image.processing.resize.BSplineFilter;
import org.openimaj.image.processing.resize.BasicFilter;
import org.openimaj.image.processing.resize.ResizeFilterFunction;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.math.geometry.shape.Rectangle;

public class NormalizedPatch {
	private final static MeanCenter msp = new MeanCenter();
	public static final int TLD_PATCH_SIZE = 15;
	public static final ResizeFilterFunction filter = new BSplineFilter();
	
	/**
	 * The slut workspace gets around a little bit. Use the slut workspace but don't expect it to be yours for long.
	 */
	public static final FImage SLUT_WORKSPACE = new FImage(TLD_PATCH_SIZE,TLD_PATCH_SIZE);
	public boolean positive;
	public FImage source;
	public Rectangle window;
	public FImage normalisedPatch;
	
	/**
	 * A function which uses {@link ResizeProcessor#zoom(FImage, Rectangle, FImage, Rectangle, ResizeFilterFunction, double)}
	 * on a to put {@link NormalizedPatch#window} form {@link NormalizedPatch#source} into holder.
	 * 
	 * This is not a convenient function but it allows for very efficient resize/normalisation process (with minimal new stuff
	 * constructed)
	 * @param holder
	 * @return the holder as a convenience 
	 */
	public FImage zoomAndNormaliseTo(FImage holder){
		ResizeProcessor.zoom(source, window,holder,holder.getBounds(),filter,filter.getDefaultSupport());
		return holder.processInline(msp);
	}

	/**
	 * calculate the variance, sets the valueImg if it is null
	 * @return an inefficient way to calculate variance of this window, a new image is constructed!
	 */
	public float calculateVariance() {
		prepareNormalisedPatch();
		float[][] value = normalisedPatch.pixels;
		float temp = 0;
	    int n = normalisedPatch.width * normalisedPatch.height;
	    for(int y = 0; y < normalisedPatch.height; y++) {
	    	for(int x = 0; x < normalisedPatch.width; x++) {
	    		temp += (value[y][x] ) * (value[y][x] ); // There are two implied (- 0)'s here. these values are MEAN CENTERED 
	    	}
	    }
	    return temp / n;
	}

	public void prepareNormalisedPatch() {
		if(this.normalisedPatch == null){
			this.normalisedPatch = new FImage(TLD_PATCH_SIZE,TLD_PATCH_SIZE);
			zoomAndNormaliseTo(normalisedPatch);
		}
	}
}
