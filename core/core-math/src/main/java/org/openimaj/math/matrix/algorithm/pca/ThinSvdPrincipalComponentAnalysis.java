package org.openimaj.math.matrix.algorithm.pca;

import org.openimaj.math.matrix.ThinSingularValueDecomposition;

import Jama.Matrix;

/**
 * Compute the PCA using a thin SVD to extract the best-n principal
 * components directly. 
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 */
public class ThinSvdPrincipalComponentAnalysis extends PrincipalComponentAnalysis {
	int ndims;
	
	/**
	 * Construct a {@link ThinSvdPrincipalComponentAnalysis} that
	 * will extract the n best eigenvectors.
	 * @param ndims the number of eigenvectors to select.
	 */
	public ThinSvdPrincipalComponentAnalysis(int ndims) {
		this.ndims = ndims;
	}
	
	@Override
	public void learnBasisNorm(Matrix data) {
		ThinSingularValueDecomposition svd = new ThinSingularValueDecomposition(data, ndims);
		basis = svd.Vt.transpose();
	}
}
