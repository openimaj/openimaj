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
package org.openimaj.demos;

import java.io.File;
import java.io.IOException;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.io.IOUtils;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Ellipse;
import org.openimaj.math.geometry.shape.EllipseUtilities;
import org.openimaj.math.statistics.distribution.MixtureOfGaussians;
import org.openimaj.math.statistics.distribution.MultivariateGaussian;

import Jama.Matrix;

public class FVFWDSiftPCAAugmentGMM {
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		// final File indir = new
		// File("/Volumes/Raid/face_databases/lfw-centre-affine-pdsift-pca64/");
		//
		// final Matrix samples = FVFWDSiftPCAAugment.sample(indir, 100000);
		//
		// final GaussianMixtureModelEM gmmem = new GaussianMixtureModelEM(512,
		// CovarianceType.Diagonal);
		// final MixtureOfGaussians mog = gmmem.estimate(samples);
		//
		// IOUtils.writeToFile(mog, new
		// File("/Volumes/Raid/face_databases/lfw-centre-affine-pdsift-pca64-augm-gmm512.bin"));
		final MixtureOfGaussians mog = IOUtils.readFromFile(new File(
				"/Volumes/Raid/face_databases/lfw-centre-affine-pdsift-pca64-augm-gmm512.bin"));

		final int w = 500;
		final int h = (int) (w * 160.0 / 125.0);

		final FImage img = new FImage(w, h);
		for (final MultivariateGaussian g : mog.gaussians) {
			final double[] mv = g.getMean().getArray()[0];
			final double x = mv[mv.length - 2] * w + (w / 2);
			final double y = mv[mv.length - 1] * h + (h / 2);

			final double xc = g.getCovariance(mv.length - 2, mv.length - 2);
			final double yc = g.getCovariance(mv.length - 1, mv.length - 1);

			final Matrix sm = new Matrix(new double[][] { { xc, 0 }, { 0, yc }
			});

			final Ellipse e = EllipseUtilities.ellipseFromCovariance((float) x,
					(float) y, sm, 500f);

			img.drawPoint(new Point2dImpl(x, y), 1f, 1);
			img.drawShape(e, 1f);
		}
		DisplayUtilities.display(img);
	}
}
