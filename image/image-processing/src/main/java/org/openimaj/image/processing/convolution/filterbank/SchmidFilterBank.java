package org.openimaj.image.processing.convolution.filterbank;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.exp;
import static java.lang.Math.sqrt;

import org.openimaj.image.FImage;
import org.openimaj.image.processing.convolution.FConvolution;

public class SchmidFilterBank extends FilterBank {
	public SchmidFilterBank() {
		this(49);
	}
	
	public SchmidFilterBank(int size) {
		this.filters = makeFilters(size);
	}

	protected FConvolution[] makeFilters(int SUP) {
		FConvolution[] F = new FConvolution[13];

		F[0] = makeFilter(SUP,2,1);
		F[1] = makeFilter(SUP,4,1);
		F[2] = makeFilter(SUP,4,2);
		F[3] = makeFilter(SUP,6,1);
		F[4] = makeFilter(SUP,6,2);
		F[5] = makeFilter(SUP,6,3);
		F[6] = makeFilter(SUP,8,1);
		F[7] = makeFilter(SUP,8,2);
		F[8] = makeFilter(SUP,8,3);
		F[9] = makeFilter(SUP,10,1);
		F[10] = makeFilter(SUP,10,2);
		F[11] = makeFilter(SUP,10,3);
		F[12] = makeFilter(SUP,10,4);

		return F;
	}

	private FConvolution makeFilter(int sup, float sigma, float tau) {
		int hs = (sup-1)/2;
		
		FImage filter = new FImage(sup, sup);
		for (int y=-hs, j=0; y<hs; y++, j++) {
			for (int x=-hs, i=0; x<hs; x++, i++) {
				float r = (float) sqrt(x*x + y*y);
				
				filter.pixels[j][i] = (float) (cos(r*(PI*tau/sigma)) * exp(-(r*r)/(2*sigma*sigma)));
			}
		}
		
		return new FConvolution(LeungMalikFilterBank.normalise(filter));
	}
}
