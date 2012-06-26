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

import org.openimaj.math.matrix.ThinSingularValueDecomposition;

import Jama.Matrix;

/**
 * Compute the PCA using a thin SVD to extract the best-n principal
 * components directly. 
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
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
		
		eigenvalues = svd.S;
		double normEig = 1.0 / (data.getRowDimension() - 1);
		for (int i=0; i<eigenvalues.length; i++) 
			eigenvalues[i] = eigenvalues[i] * eigenvalues[i] * normEig;
	}
}
