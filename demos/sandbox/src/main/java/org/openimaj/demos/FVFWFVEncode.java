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
