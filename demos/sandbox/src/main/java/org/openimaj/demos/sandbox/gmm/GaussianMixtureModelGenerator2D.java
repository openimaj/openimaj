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
package org.openimaj.demos.sandbox.gmm;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Ellipse;
import org.openimaj.math.geometry.shape.EllipseUtilities;
import org.openimaj.math.matrix.MatrixUtils;
import org.openimaj.math.statistics.distribution.CachingMultivariateGaussian;
import org.openimaj.math.statistics.distribution.MultivariateGaussian;

import Jama.CholeskyDecomposition;
import Jama.Matrix;

public class GaussianMixtureModelGenerator2D implements GaussianMixtureModelGenerator {

	private static final int N_POINTS = 200;
	private List<CachingMultivariateGaussian> normList;
	private Random random;
	private double[] pi;

	private GaussianMixtureModelGenerator2D() {
		normList = new ArrayList<CachingMultivariateGaussian>();
		this.random = new Random();
	}

	/**
	 * @param ellipses
	 */
	public GaussianMixtureModelGenerator2D(Ellipse... ellipses) {
		this();
		for (final Ellipse ellipse : ellipses) {
			final Matrix mean = new Matrix(1, 2);
			final Point2d cog = ellipse.getCOG();
			final Ellipse corrected = new Ellipse(cog.getX(), cog.getY(), ellipse.getMajor() / 2, ellipse.getMinor() / 2,
					ellipse.getRotation());
			final Matrix covar = EllipseUtilities.ellipseToCovariance(corrected);
			mean.set(0, 0, cog.getX());
			mean.set(0, 1, cog.getY());

			normList.add(new CachingMultivariateGaussian(mean, covar));
		}

		this.pi = new double[normList.size()];
		for (int i = 0; i < pi.length; i++) {
			pi[i] = 1f / pi.length;
		}
	}

	@Override
	public Generated generate() {
		final Generated g = new Generated();
		final double probZ = random.nextDouble();
		double sum = 0;
		g.distribution = this.pi.length - 1;
		for (int i = 0; i < this.pi.length; i++) {
			sum += pi[i];
			if (sum > probZ) {
				g.distribution = i;
				break;
			}
		}

		final MultivariateGaussian distrib = this.normList.get(g.distribution);
		final Matrix mean = distrib.getMean().transpose();
		final Matrix covar = distrib.getCovariance();
		final CholeskyDecomposition decomp = new CholeskyDecomposition(covar);
		final Matrix r = MatrixUtils.randGaussian(2, 1);

		final Matrix genPoint = mean.plus(decomp.getL().times(r));
		g.point = new double[] { genPoint.get(0, 0), genPoint.get(1, 0) };
		g.responsibilities = new double[this.pi.length];

		sum = 0;
		for (int i = 0; i < g.responsibilities.length; i++) {
			sum += g.responsibilities[i] = this.normList.get(i).estimateProbability(g.point);
		}

		for (int i = 0; i < g.responsibilities.length; i++) {
			g.responsibilities[i] /= sum;
		}

		return g;
	}

	public static void main(String[] args) {
		final Ellipse e1 = new Ellipse(200, 200, 40, 20, Math.PI / 3);
		final Ellipse e2 = new Ellipse(220, 150, 60, 20, -Math.PI / 3);
		final Ellipse e3 = new Ellipse(180, 200, 80, 20, -Math.PI / 3);
		final Float[][] colours = new Float[][] { RGBColour.RED, RGBColour.GREEN, RGBColour.BLUE };
		final MBFImage image = new MBFImage(400, 400, 3);
		image.drawShape(e1, RGBColour.RED);
		image.drawShape(e2, RGBColour.GREEN);
		image.drawShape(e3, RGBColour.BLUE);

		final GaussianMixtureModelGenerator2D gmm = new GaussianMixtureModelGenerator2D(e1, e2, e3);
		final MBFImage imageUnblended = image.clone();
		final MBFImage imageBlended = image.clone();
		for (int i = 0; i < N_POINTS; i++) {
			final Generated gen = gmm.generate();
			final Point2d p = new Point2dImpl((float) gen.point[0], (float) gen.point[1]);
			imageUnblended.drawPoint(p, colours[gen.distribution], 3);
			final Float[] weightedColour = new Float[3];
			for (int j = 0; j < weightedColour.length; j++) {
				weightedColour[j] = 0f;
			}
			for (int colour = 0; colour < colours.length; colour++) {
				for (int channel = 0; channel < colours[colour].length; channel++) {
					weightedColour[channel] = (float) (weightedColour[channel] + colours[colour][channel]
							* gen.responsibilities[colour]);
				}
			}
			double sumWeight = 0;
			for (int j = 0; j < weightedColour.length; j++) {
				sumWeight += weightedColour[j];
			}
			for (int j = 0; j < weightedColour.length; j++) {
				weightedColour[j] = (float) (weightedColour[j] / sumWeight);
			}
			imageBlended.drawPoint(p, weightedColour, 3);
		}

		DisplayUtilities.display(imageUnblended);
		DisplayUtilities.display(imageBlended);

	}

	@Override
	public int dimensions() {
		return 2;
	}

}
