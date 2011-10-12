package org.openimaj.image.processing.convolution;

import org.openimaj.image.FImage;
import org.openimaj.math.util.FloatArrayStatsUtils;

/**
 * Disk - a circular averaging filter.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class Disk extends FConvolution {
	
	/**
	 * Default constructor. Makes a disk averaging filter
	 * with the given radius.
	 * @param radius the radius.
	 */
	public Disk(int radius) {
		super(createKernelImage(radius));
	}

	/**
	 * Makes a disk averaging filter with the given radius.
	 * @param radius
	 * @return the filter image
	 */
	public static FImage createKernelImage(int radius) {
		int sze = 2*radius+1;
		FImage f = new FImage(sze, sze);
		int hsz = (sze-1)/2;
		
		for (int y=-hsz, j=0; y<hsz; y++, j++) {
			for (int x=-hsz, i=0; x<hsz; x++, i++) {
				double rad = Math.sqrt(x*x + y*y);
				f.pixels[j][i] = rad < radius ? 1 : 0; 				
			}
		}
		float sum = FloatArrayStatsUtils.sum(f.pixels);
		return f.divideInline(sum);		
	}
}
