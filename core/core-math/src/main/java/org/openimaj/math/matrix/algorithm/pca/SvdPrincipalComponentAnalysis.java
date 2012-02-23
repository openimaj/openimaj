package org.openimaj.math.matrix.algorithm.pca;

import java.util.Arrays;

import no.uib.cipr.matrix.NotConvergedException;
import Jama.Matrix;


/**
 * Compute the PCA using an SVD without actually constructing
 * the covariance matrix. This class performs a full SVD extracting all
 * singular values and vectors. If you know apriori how many principle
 * components (or have an upper bound on the number), then use a
 * {@link ThinSvdPrincipalComponentAnalysis} instead. 
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 */
public class SvdPrincipalComponentAnalysis extends PrincipalComponentAnalysis {
	int ndims;
	
	/**
	 * Construct a {@link SvdPrincipalComponentAnalysis} that
	 * will extract all the eigenvectors.
	 */
	public SvdPrincipalComponentAnalysis() {
		this(-1);
	}
	
	/**
	 * Construct a {@link SvdPrincipalComponentAnalysis} that
	 * will extract the n best eigenvectors.
	 * @param ndims the number of eigenvectors to select.
	 */
	public SvdPrincipalComponentAnalysis(int ndims) {
		this.ndims = ndims;
	}
	
	@Override
	public void learnBasisNorm(Matrix norm) {
		try {
			no.uib.cipr.matrix.DenseMatrix mjtA = new no.uib.cipr.matrix.DenseMatrix(norm.getArray());
			no.uib.cipr.matrix.SVD svd = no.uib.cipr.matrix.SVD.factorize(mjtA);
			
			no.uib.cipr.matrix.DenseMatrix output = svd.getVt();

			int dims = ndims < 0 ? svd.getS().length : ndims;

			basis = new Matrix(output.numColumns(), dims);
			eigenvalues = Arrays.copyOf(svd.getS(), dims);
			
			double normEig = 1.0 / (norm.getRowDimension() - 1);
			for (int i=0; i<eigenvalues.length; i++) 
				eigenvalues[i] = eigenvalues[i] * eigenvalues[i] * normEig;
			
			double[][] basisData = basis.getArray();
			for (int j=0; j<output.numColumns(); j++)
				for (int i=0; i<dims; i++)
					basisData[j][i] = output.get(i, j);
			
		} catch (NotConvergedException e) {
			throw new RuntimeException(e);
		}
	}
}
