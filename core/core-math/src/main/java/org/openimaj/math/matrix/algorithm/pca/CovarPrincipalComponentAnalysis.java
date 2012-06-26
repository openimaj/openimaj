/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
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
		MatrixUtils.reverseColumnsInplace(basis);
		
		//swap evals
		ArrayUtils.reverse(eigenvalues);
	}
}
