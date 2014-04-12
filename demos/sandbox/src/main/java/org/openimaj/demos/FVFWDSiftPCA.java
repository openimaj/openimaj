package org.openimaj.demos;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.image.feature.dense.gradient.dsift.FloatDSIFTKeypoint;
import org.openimaj.io.IOUtils;
import org.openimaj.math.matrix.algorithm.pca.ThinSvdPrincipalComponentAnalysis;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.util.function.Operation;
import org.openimaj.util.parallel.Parallel;

import scala.actors.threadpool.Arrays;
import Jama.Matrix;

public class FVFWDSiftPCA {
	static Matrix sample(File dir, int nsamples) throws IOException {
		final List<File> files = new ArrayList<File>();

		System.out.println("Finding files");
		for (final File d : dir.listFiles()) {
			if (d.isDirectory()) {
				for (final File f : d.listFiles()) {
					if (f.getName().endsWith(".bin")) {
						files.add(f);
					}
				}
			}
		}

		System.out.println("Shuffling");
		Collections.shuffle(files);

		System.out.println("Sampling");
		final double[][] data = new double[nsamples][];
		final int nPerFile = (int) Math.ceil(nsamples / (double) files.size());
		final List<FloatDSIFTKeypoint> samples = new ArrayList<FloatDSIFTKeypoint>();

		Parallel.forEach(files, new Operation<File>() {
			@Override
			public void perform(File f) {
				try {
					System.out.println(f);

					final MemoryLocalFeatureList<FloatDSIFTKeypoint> feats = MemoryLocalFeatureList.read(f,
							FloatDSIFTKeypoint.class);

					Collections.shuffle(feats);

					final MemoryLocalFeatureList<FloatDSIFTKeypoint> s = feats.subList(0, nPerFile);
					synchronized (samples) {
						samples.addAll(s);
					}
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		});

		for (int i = 0; i < nsamples; i++) {
			data[i] = ArrayUtils.convertToDouble(samples.get(i).descriptor);
		}

		System.out.println("Done");

		return new Matrix(data);
	}

	@SuppressWarnings("unchecked")
	private static void processFiles(final ThinSvdPrincipalComponentAnalysis pca, final File indir, final File outdir)
			throws IOException
	{
		for (final File d : indir.listFiles()) {
			if (d.isDirectory()) {

				Parallel.forEach(Arrays.asList(d.listFiles()), new Operation<File>() {
					@Override
					public void perform(File f) {
						if (f.getName().endsWith(".bin")) {
							try {
								System.out.println("Processing " + f);

								final MemoryLocalFeatureList<FloatDSIFTKeypoint> feats = MemoryLocalFeatureList.read(f,
										FloatDSIFTKeypoint.class);

								final File outfile = new File(outdir, f.getAbsolutePath().replace(
										indir.getAbsolutePath(), ""));
								outfile.getParentFile().mkdirs();

								for (final FloatDSIFTKeypoint kpt : feats) {
									final double[] proj = pca.project(ArrayUtils.convertToDouble(kpt.descriptor));
									kpt.descriptor = ArrayUtils.convertToFloat(proj);
								}

								IOUtils.writeBinary(outfile, feats);
							} catch (final Exception e) {

							}
						}
					}
				});
			}
		}
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		final File indir = new File("/Volumes/Raid/face_databases/lfw-centre-affine-pdsift/");
		// final Matrix samples = sample(indir, 100000);
		//
		// final ThinSvdPrincipalComponentAnalysis pca = new
		// ThinSvdPrincipalComponentAnalysis(64);
		// System.out.println("Performing PCA");
		// pca.learnBasis(samples);
		// IOUtils.writeToFile(pca, new
		// File("/Volumes/Raid/face_databases/lfw-centre-affine-pdsift-pca64.bin"));
		final ThinSvdPrincipalComponentAnalysis pca = IOUtils.readFromFile(new File(
				"/Volumes/Raid/face_databases/lfw-centre-affine-pdsift-pca64.bin"));

		processFiles(pca, indir, new File("/Volumes/Raid/face_databases/lfw-centre-affine-pdsift-pca64/"));
	}
}
