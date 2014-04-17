package org.openimaj.demos;

import java.io.File;
import java.io.IOException;

import org.openimaj.io.IOUtils;
import org.openimaj.math.statistics.distribution.MixtureOfGaussians;
import org.openimaj.ml.gmm.GaussianMixtureModelEM;
import org.openimaj.ml.gmm.GaussianMixtureModelEM.CovarianceType;

import Jama.Matrix;

public class FVFWDSiftPCAAugmentGMM {
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		final File indir = new File("/Volumes/Raid/face_databases/lfw-centre-affine-pdsift-pca64-augm/");

		final Matrix samples = FVFWDSiftPCAAugment.sample(indir, 100000);

		final GaussianMixtureModelEM gmmem = new GaussianMixtureModelEM(512, CovarianceType.Diagonal);
		final MixtureOfGaussians mog = gmmem.estimate(samples);

		IOUtils.writeToFile(mog, new File("/Volumes/Raid/face_databases/lfw-centre-affine-pdsift-pca64-augm-gmm512.bin"));
	}
}
