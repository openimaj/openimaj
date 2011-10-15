package org.openimaj.image.processing.convolution.filterbank;

import static java.lang.Math.PI;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.processing.convolution.FConvolution;
import org.openimaj.image.processing.convolution.Gaussian2D;
import org.openimaj.image.processing.convolution.LaplacianOfGaussian2D;

public class RootFilterSetFilterBank extends FilterBank {
	protected final static float [] SCALEX = {1,2,4};
	protected final static int NORIENT = 6;
	
	public RootFilterSetFilterBank() {
		this(49);
	}

	public RootFilterSetFilterBank(int size) {
		this.filters = makeFilters(size);
	}

	protected FConvolution[] makeFilters(int size) {

		int NROTINV = 2;
		int NBAR = SCALEX.length * NORIENT;
		int NEDGE = SCALEX.length * NORIENT;
		int NF = NBAR + NEDGE + NROTINV;
		FConvolution [] F = new FConvolution[NF];

		int count=0;
		for (int scale=0; scale<SCALEX.length; scale++) {
			for (int orient=0; orient<NORIENT; orient++) {
				float angle = (float) (PI * orient / NORIENT);
				F[count] = new FConvolution(LeungMalikFilterBank.makeFilter(scale, 0, 1, angle, size));
				F[count + NEDGE] = new FConvolution(LeungMalikFilterBank.makeFilter(scale, 0, 2, angle, size));
				count++;
			}
		}  

		F[NBAR+NEDGE] = new FConvolution(LeungMalikFilterBank.normalise(Gaussian2D.createKernelImage(size, 10)));
		F[NBAR+NEDGE+1] = new FConvolution(LeungMalikFilterBank.normalise(LaplacianOfGaussian2D.createKernelImage(size, 10)));

		return F;
	}
}
