package org.openimaj.demos;

import java.io.File;
import java.io.IOException;

import org.openimaj.feature.FloatFV;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.image.feature.dense.gradient.dsift.FloatDSIFTKeypoint;
import org.openimaj.image.feature.local.aggregate.FisherVector;
import org.openimaj.io.IOUtils;
import org.openimaj.math.statistics.distribution.MixtureOfGaussians;
import org.openimaj.util.function.Operation;
import org.openimaj.util.parallel.Parallel;

import scala.actors.threadpool.Arrays;

public class FVFWFVEncode {
	/**
	 * @param args
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException {
		final MixtureOfGaussians gmm = IOUtils.readFromFile(new File(
				"/Volumes/Raid/face_databases/lfw-centre-affine-pdsift-pca64-augm-gmm512.bin"));
		final FisherVector<float[]> fisher = new FisherVector<float[]>(gmm, true, true);
		IOUtils.writeToFile(fisher, new File(
				"/Volumes/Raid/face_databases/lfw-centre-affine-pdsift-pca64-augm-gmm512-fisher.bin"));

		final File indir = new File("/Volumes/Raid/face_databases/lfw-centre-affine-pdsift-pca64/");
		final File outdir = new File("/Volumes/Raid/face_databases/lfw-centre-affine-pdsift-pca64-augm-fv512/");

		Parallel.forEach(Arrays.asList(indir.listFiles()), new Operation<File>() {
			@Override
			public void perform(File dir) {
				if (dir.isDirectory()) {
					for (final File f : dir.listFiles()) {
						if (f.getName().endsWith(".bin")) {
							try {
								System.out.println(f);

								final MemoryLocalFeatureList<FloatDSIFTKeypoint> feats = MemoryLocalFeatureList.read(f,
										FloatDSIFTKeypoint.class);

								final File outfile = new File(outdir, f.getAbsolutePath().replace(
										indir.getAbsolutePath(), ""));
								outfile.getParentFile().mkdirs();

								final FloatFV fv = fisher.aggregate(feats);
								IOUtils.writeBinary(outfile, fv);
							} catch (final Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		});
	}
}
