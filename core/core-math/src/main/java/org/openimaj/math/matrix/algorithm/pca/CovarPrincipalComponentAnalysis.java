package org.openimaj.math.matrix.algorithm.pca;

import org.openimaj.math.matrix.MatrixUtils;
import org.openimaj.util.array.ArrayUtils;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;


/**
 * Naive Principle Component Analysis performed by directly calculating 
 * the covariance matrix and then performing an Eigen decomposition.
 * 
 * This implementation should not be used in general as it is expensive.
 * The {@link SvdPrincipalComponentAnalysis} and {@link ThinSvdPrincipalComponentAnalysis}
 * implementations are much faster and more efficient.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class CovarPrincipalComponentAnalysis extends PrincipalComponentAnalysis {
	int ndims;
	
	/**
	 * Construct a {@link CovarPrincipalComponentAnalysis} that
	 * will extract all the eigenvectors.
	 */
	public CovarPrincipalComponentAnalysis() {
		this(-1);
	}
	
	/**
	 * Construct a {@link CovarPrincipalComponentAnalysis} that
	 * will extract the n best eigenvectors.
	 * @param ndims the number of eigenvectors to select.
	 */
	public CovarPrincipalComponentAnalysis(int ndims) {
		this.ndims = ndims;
	}
	
	@Override
	protected void learnBasisNorm(Matrix m) {
		Matrix covar = m.transpose().times(m);
		
		EigenvalueDecomposition eig = covar.eig();
		Matrix all_eigenvectors = eig.getV();
		
		//note eigenvalues are in increasing order, so last vec is first pc
		if (ndims > 0)
			basis = all_eigenvectors.getMatrix(0, all_eigenvectors.getRowDimension()-1, Math.max(0, all_eigenvectors.getColumnDimension() - ndims), all_eigenvectors.getColumnDimension()-1);
		else 
			basis = all_eigenvectors;
		
		eigenvalues = eig.getRealEigenvalues();
		double norm = 1.0 / (m.getRowDimension() - 1);
		for (int i=0; i<eigenvalues.length; i++) eigenvalues[i] *= norm;
		
		//swap evecs
		MatrixUtils.reverseColumnsInline(basis);
		
		//swap evals
		ArrayUtils.reverse(eigenvalues);
	}
}
