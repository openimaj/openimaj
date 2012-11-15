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
package org.openimaj.ml.pca;

import java.util.Collection;

import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureVector;
import org.openimaj.math.matrix.algorithm.pca.PrincipalComponentAnalysis;
import org.openimaj.math.matrix.algorithm.pca.SvdPrincipalComponentAnalysis;

import Jama.Matrix;

/**
 * Principal Components Analysis wrapper for {@link FeatureVector}s.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class FeatureVectorPCA extends PrincipalComponentAnalysis {
	PrincipalComponentAnalysis inner;

	/**
	 * Default constructor, using an {@link SvdPrincipalComponentAnalysis}.
	 */
	public FeatureVectorPCA() {
		this.inner = new SvdPrincipalComponentAnalysis();
	}

	/**
	 * Construct with the given {@link PrincipalComponentAnalysis} object.
	 * 
	 * @param inner
	 *            PCA algorithm.
	 */
	public FeatureVectorPCA(PrincipalComponentAnalysis inner) {
		this.inner = inner;
	}

	/**
	 * Learn the PCA basis of the given feature vectors.
	 * 
	 * @param data
	 *            the feature vectors to apply PCA to.
	 */
	public void learnBasis(FeatureVector[] data) {
		final double[][] d = new double[data.length][];

		for (int i = 0; i < data.length; i++) {
			d[i] = data[i].asDoubleVector();
		}

		learnBasis(d);
	}

	/**
	 * Learn the PCA basis of the given feature vectors.
	 * 
	 * @param data
	 *            the feature vectors to apply PCA to.
	 */
	public void learnBasis(Collection<? extends FeatureVector> data) {
		final double[][] d = new double[data.size()][];

		int i = 0;
		for (final FeatureVector fv : data) {
			d[i++] = fv.asDoubleVector();
		}

		learnBasis(d);
	}

	/**
	 * Project a vector by the basis. The vector is normalised by subtracting
	 * the mean and then multiplied by the basis.
	 * 
	 * @param vector
	 *            the vector to project
	 * @return projected vector
	 */
	public DoubleFV project(FeatureVector vector) {
		return new DoubleFV(project(vector.asDoubleVector()));
	}

	@Override
	public void learnBasis(double[][] data) {
		inner.learnBasis(data);
		this.basis = inner.getBasis();
		this.eigenvalues = inner.getEigenValues();
		this.mean = inner.getMean();
	}

	@Override
	protected void learnBasisNorm(Matrix norm) {
		inner.learnBasis(norm);
	}

	/**
	 * Generate a new "observation" as a linear combination of the principal
	 * components (PC): mean + PC * scaling.
	 * 
	 * If the scaling vector is shorter than the number of components, it will
	 * be zero-padded. If it is longer, it will be truncated.
	 * 
	 * @param scalings
	 *            the weighting for each PC
	 * @return generated observation
	 */
	public DoubleFV generate(DoubleFV scalings) {
		return new DoubleFV(generate(scalings.values));
	}
}
