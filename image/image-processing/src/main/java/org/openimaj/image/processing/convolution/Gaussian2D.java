package org.openimaj.image.processing.convolution;

import static java.lang.Math.exp;

import org.openimaj.image.FImage;
import org.openimaj.math.util.FloatArrayStatsUtils;

/**
 * Simple 2D Gaussian convolution. In most cases the {@link FGaussianConvolve}
 * filter will do the same thing, but much much faster!
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class Gaussian2D extends FConvolution {

	public Gaussian2D(int width, int height, float sigma) {
		super(createKernelImage(width, height, sigma));
	}
	
	public Gaussian2D(int size, float sigma) {
		super(createKernelImage(size, size, sigma));
	}

	public static FImage createKernelImage(int size, float sigma) {
		return createKernelImage(size, size, sigma);
	}
	
	public static FImage createKernelImage(int width, int height, float sigma) {
		FImage f = new FImage(width, height);
		int hw = (width-1)/2;
		int hh = (height-1)/2;
		float sigmasq = sigma * sigma;
		
		for (int y=-hh, j=0; y<hh; y++, j++) {
			for (int x=-hw, i=0; x<hw; x++, i++) {
				int radsqrd = x*x + y*y;
				f.pixels[j][i] = (float) exp( -radsqrd/ ( 2 * sigmasq ) );				
			}
		}
		float sum = FloatArrayStatsUtils.sum(f.pixels);
		return f.divideInline(sum);
	}
}
