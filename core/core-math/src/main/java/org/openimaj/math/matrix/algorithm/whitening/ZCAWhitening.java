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
package org.openimaj.math.matrix.algorithm.whitening;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.math.matrix.algorithm.pca.SvdPrincipalComponentAnalysis;
import org.openimaj.math.statistics.normalisation.Normaliser;

import Jama.Matrix;

/**
 * The ZCA Whitening transform. Works like PCA whitening, but after variance
 * normalisation, rotates the data back to the original orientation. The benefit
 * is that the data will still "look-like" the original input to some extent.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Reference(
		type = ReferenceType.Article,
		author = { "Anthony J. Bell", "Terrence J. Sejnowski" },
		title = "The `Independent Components' of Natural Scenes are Edge Filters.",
		year = "1997",
		journal = "VISION RESEARCH",
		pages = { "3327", "", "3338" },
		volume = "37")
public class ZCAWhitening extends PCAWhitening {
	/**
	 * Construct with the given variance regularization parameter and data
	 * normalisation strategy.
	 *
	 * @param eps
	 *            the variance normalisation regularizer (each principle
	 *            dimension is divided by sqrt(lamba + eps), where lamba is the
	 *            corresponding eigenvalue).
	 * @param ns
	 *            the normalisation to apply to each input data vector prior to
	 *            training the transform or applying the actual whitening.
	 */
	public ZCAWhitening(double eps, Normaliser ns) {
		super(eps, ns);
	}

	@Override
	public void train(double[][] data) {
		ns.normalise(data);
		final double[][] normData = ns.normalise(data);

		final SvdPrincipalComponentAnalysis pca = new SvdPrincipalComponentAnalysis();
		pca.learnBasisNorm(new Matrix(normData));
		transform = pca.getBasis().copy();
		final double[] weight = pca.getEigenValues();
		final double[][] td = transform.getArray();

		for (int c = 0; c < weight.length; c++)
			weight[c] = 1 / Math.sqrt(weight[c] + eps);

		for (int r = 0; r < td.length; r++)
			for (int c = 0; c < td[0].length; c++)
				td[r][c] = td[r][c] * weight[c];

		transform = transform.times(pca.getBasis().transpose());
	}
}
