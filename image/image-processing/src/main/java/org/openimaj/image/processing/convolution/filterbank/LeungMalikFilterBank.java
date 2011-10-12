package org.openimaj.image.processing.convolution.filterbank;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.exp;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import org.openimaj.image.FImage;
import org.openimaj.image.processing.convolution.FConvolution;
import org.openimaj.image.processing.convolution.Gaussian2D;
import org.openimaj.image.processing.convolution.LaplacianOfGaussian2D;
import org.openimaj.math.util.FloatArrayStatsUtils;

/**
 * Implementation of a the filter bank described in:
 * T. Leung and J. Malik. Representing and recognizing the visual 
 * appearance of materials using three-dimensional textons. IJCV, 2001
 * 
 * Inspired by the matlab implementation from 
 * http://www.robots.ox.ac.uk/~vgg/research/texclass/filters.html
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 */
public class LeungMalikFilterBank extends FilterBank {
	public LeungMalikFilterBank() {
		this(49);
	}
	
	public LeungMalikFilterBank(int size) {
		this.filters = makeFilters(size);
	}

	protected FConvolution[] makeFilters(int size) {
		final int nScales = 3;
		final int nOrientations = 6;

		int NROTINV = 12;
		int NBAR = nScales * nOrientations;
		int NEDGE = nScales * nOrientations;
		int NF = NBAR + NEDGE + NROTINV;

		FConvolution F[] = new FConvolution[NF];

		int count=0;
		for (int i=1; i<=nScales; i++) {
			float scale = (float) pow(sqrt(2),i);
			
			for (int orient=0; orient<nOrientations; orient++) {
				float angle = (float) (PI * orient / nOrientations);
				
				F[count] = new FConvolution(makeFilter(scale, 0, 1, angle, size));
				F[count + NEDGE] = new FConvolution(makeFilter(scale, 0, 2, angle, size));
				count++;
			}
		}

		count=NBAR+NEDGE;
		for (int i=1; i<=4; i++) {
			float scale = (float) pow(sqrt(2), i);
			
			F[count] = new FConvolution(normalise(Gaussian2D.createKernelImage(size, scale)));
			F[count + 1] = new FConvolution(normalise(LaplacianOfGaussian2D.createKernelImage(size, scale)));
			F[count + 2] = new FConvolution(normalise(LaplacianOfGaussian2D.createKernelImage(size, 3 * scale)));
			count+=3;
		}
		
		return F;
	}

	protected static FImage makeFilter(float scale, int phasex, int phasey, float angle, int size) {
		int hs = (size-1)/2;

		FImage filter = new FImage(size, size);
		for (int y=-hs, j=0; y<hs; y++, j++) {
			for (int x=-hs, i=0; x<hs; x++, i++) {
				float cos=(float) cos(angle);
				float sin=(float) sin(angle);

				float rx = cos*x - sin*y;
				float ry = sin*x + cos*y;

				float gx = gaussian1D(3*scale, 0, rx, phasex);
				float gy = gaussian1D(scale, 0, ry, phasey);

				filter.pixels[j][i] = gx * gy;
			}
		}

		return normalise(filter);
	}

	protected static float gaussian1D(float sigma, float mean, float x, int order) {	
		x=x-mean;
		float num = x*x;

		float variance = sigma*sigma;
		float denom = 2*variance;  
		float g = (float) (exp(-num/denom)/pow(PI*denom,0.5));
		
		switch(order) {
			case 0: return g;
			case 1: return -g * (x/variance);
			case 2: return g * ((num-variance)/(variance*variance));
			default:
				throw new IllegalArgumentException("order must be 0, 1 or 2.");
		}
	}

	protected static FImage normalise(FImage f) {
		float mean = FloatArrayStatsUtils.mean(f.pixels);
		f.subtractInline(mean);
		float sumabs = FloatArrayStatsUtils.sumAbs(f.pixels);
		return f.divideInline(sumabs);
	}
}
