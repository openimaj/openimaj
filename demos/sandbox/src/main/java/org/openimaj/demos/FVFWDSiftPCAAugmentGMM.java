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
