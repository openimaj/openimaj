package org.openimaj.image.processing.convolution.filterbank;

import static java.lang.Math.PI;

import org.openimaj.image.processing.convolution.FConvolution;
import org.openimaj.image.processing.convolution.Gaussian2D;
import org.openimaj.image.processing.convolution.LaplacianOfGaussian2D;

/**
 * Implementation of the Root Filter Set filter bank described at:
 * http://www.robots.ox.ac.uk/~vgg/research/texclass/filters.html
 *  
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 */

public class RootFilterSetFilterBank extends FilterBank {
	protected final static float [] SCALES = {1,2,4};
	protected final static int NUM_ORIENTATIONS = 6;
	
	public RootFilterSetFilterBank() {
		this(49);
	}

	public RootFilterSetFilterBank(int size) {
		this.filters = makeFilters(size);
	}

	protected FConvolution[] makeFilters(int size) {
		int numRotInvariants = 2;
		int numBar = SCALES.length * NUM_ORIENTATIONS;
		int numEdge = SCALES.length * NUM_ORIENTATIONS;
		int numFilters = numBar + numEdge + numRotInvariants;
		FConvolution [] F = new FConvolution[numFilters];

		int count=0;
		for (float scale : SCALES) {
			for (int orient=0; orient<NUM_ORIENTATIONS; orient++) {
				float angle = (float) (PI * orient / NUM_ORIENTATIONS);
				
				F[count] = new FConvolution(LeungMalikFilterBank.makeFilter(scale, 0, 1, angle, size));
				F[count + numEdge] = new FConvolution(LeungMalikFilterBank.makeFilter(scale, 0, 2, angle, size));
				count++;
			}
		}  

		F[numBar+numEdge] = new FConvolution(Gaussian2D.createKernelImage(size, 10)); //don't normalise
		F[numBar+numEdge+1] = new FConvolution(LeungMalikFilterBank.normalise(LaplacianOfGaussian2D.createKernelImage(size, 10)));

		return F;
	}
}
