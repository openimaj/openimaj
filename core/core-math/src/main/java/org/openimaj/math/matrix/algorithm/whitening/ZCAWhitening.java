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
