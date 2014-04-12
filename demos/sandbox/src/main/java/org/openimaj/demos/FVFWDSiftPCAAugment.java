package org.openimaj.demos;

import java.io.File;
import java.io.IOException;

import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.image.feature.dense.gradient.dsift.FloatDSIFTKeypoint;
import org.openimaj.io.IOUtils;
import org.openimaj.util.function.Operation;
import org.openimaj.util.parallel.Parallel;

import scala.actors.threadpool.Arrays;

public class FVFWDSiftPCAAugment {
	@SuppressWarnings("unchecked")
	private static void processFiles(final File indir, final File outdir)
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
									kpt.descriptor = Arrays.copyOf(kpt.descriptor, kpt.descriptor.length + 2);
									kpt.descriptor[kpt.descriptor.length - 2] = (kpt.x / 125f) - 0.5f;
									kpt.descriptor[kpt.descriptor.length - 1] = (kpt.y / 160f) - 0.5f;
								}

								IOUtils.writeBinary(outfile, feats);
							} catch (final Exception e) {
								e.printStackTrace();
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
		final File indir = new File("/Volumes/Raid/face_databases/lfw-centre-affine-pdsift-pca64/");
		processFiles(indir, new File("/Volumes/Raid/face_databases/lfw-centre-affine-pdsift-pca64-augm/"));
	}
}
