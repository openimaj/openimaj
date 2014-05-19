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

import org.openimaj.demos.FVFWDSift.DSFactory;
import org.openimaj.feature.FloatFV;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.analysis.pyramid.SimplePyramid;
import org.openimaj.image.feature.dense.gradient.dsift.ApproximateDenseSIFT;
import org.openimaj.image.feature.dense.gradient.dsift.ByteDSIFTKeypoint;
import org.openimaj.image.feature.dense.gradient.dsift.DenseSIFT;
import org.openimaj.image.feature.dense.gradient.dsift.FloatDSIFTKeypoint;
import org.openimaj.image.feature.local.aggregate.FisherVector;
import org.openimaj.io.IOUtils;
import org.openimaj.math.matrix.algorithm.pca.PrincipalComponentAnalysis;
import org.openimaj.math.statistics.distribution.MixtureOfGaussians;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.util.function.Operation;
import org.openimaj.util.parallel.Parallel;

import scala.actors.threadpool.Arrays;

public class FVFWFVEncodeMatlab {
	/**
	 * @param args
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException {
		final MixtureOfGaussians gmm = FVFWCheckPCAGMM.loadMoG(new File(
				"/Users/jon/Downloads/data/lfw_aligned/SIFT_1pix_PCA64_GMM512/codebooks/1/gmm_512.mat"));
		final PrincipalComponentAnalysis pca = FVFWCheckPCAGMM.loadPCA(new File(
				"/Users/jon/Downloads/data/lfw_aligned/SIFT_1pix_PCA64_GMM512/codebooks/1/PCA_64.mat"));

		final FisherVector<float[]> fisher = new FisherVector<float[]>(gmm, true, true);

		final DSFactory factory = new DSFactory() {
			@Override
			public DenseSIFT create() {
				return new ApproximateDenseSIFT(1, 6);
			}
		};

		final File indir = new File("/Volumes/Raid/face_databases/lfw-centre-affine-matlab/");
		final File outdir = new File("/Volumes/Raid/face_databases/lfw-centre-affine-matlab-fisher/");

		Parallel.forEach(Arrays.asList(indir.listFiles()), new Operation<File>() {
			@Override
			public void perform(File dir) {
				if (dir.isDirectory()) {
					final DenseSIFT sift = factory.create();

					for (final File f : dir.listFiles()) {
						if (f.getName().endsWith(".jpg")) {
							try {
								final File outfile = new File(outdir, f.getAbsolutePath().replace(
										indir.getAbsolutePath(), "").replace(".jpg", ".bin"));
								outfile.getParentFile().mkdirs();

								if (outfile.exists())
									continue;

								System.out.println(f);

								final LocalFeatureList<FloatDSIFTKeypoint> features = computeFeatures(f, pca, sift);

								final FloatFV fv = fisher.aggregate(features);
								IOUtils.writeBinary(outfile, fv);
							} catch (final Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			}

			private LocalFeatureList<FloatDSIFTKeypoint> computeFeatures(File f, PrincipalComponentAnalysis pca,
					DenseSIFT sift) throws IOException
			{
				final FImage image = ImageUtilities.readF(f);

				final SimplePyramid<FImage> pyr = new SimplePyramid<FImage>((float) Math.sqrt(2), 5);
				pyr.processImage(image);

				final LocalFeatureList<FloatDSIFTKeypoint> allKeys = new MemoryLocalFeatureList<FloatDSIFTKeypoint>();
				for (final FImage img : pyr) {
					sift.analyseImage(img);

					final double scale = 160.0 / img.height;
					final LocalFeatureList<ByteDSIFTKeypoint> kps = sift.getByteKeypoints();
					for (final ByteDSIFTKeypoint kp : kps) {
						kp.x = (float) ((kp.x + 1) * scale);
						kp.y = (float) ((kp.y + 1) * scale);

						float[] descriptor = new float[128];
						float sumsq = 0;

						// reorder to make comparision with matlab
						// easier; add offset
						for (int i = 0; i < 16; i++) {
							descriptor[i * 8] = kp.descriptor[i * 8] + 128;
							descriptor[i * 8 + 1] = kp.descriptor[i * 8 + 7] + 128;
							descriptor[i * 8 + 2] = kp.descriptor[i * 8 + 6] + 128;
							descriptor[i * 8 + 3] = kp.descriptor[i * 8 + 5] + 128;
							descriptor[i * 8 + 4] = kp.descriptor[i * 8 + 4] + 128;
							descriptor[i * 8 + 5] = kp.descriptor[i * 8 + 3] + 128;
							descriptor[i * 8 + 6] = kp.descriptor[i * 8 + 2] + 128;
							descriptor[i * 8 + 7] = kp.descriptor[i * 8 + 1] + 128;
						}
						// rootsift
						for (int i = 0; i < 128; i++) {
							descriptor[i] = (float) Math.sqrt(descriptor[i]);
							sumsq += descriptor[i] * descriptor[i];
						}
						sumsq = (float) Math.sqrt(sumsq);
						final float norm = 1f / Math.max(Float.MIN_NORMAL, sumsq);
						for (int i = 0; i < 128; i++) {
							descriptor[i] *= norm;
						}

						// PCA
						descriptor = ArrayUtils.convertToFloat(pca.project(ArrayUtils.convertToDouble(descriptor)));

						// Augment
						final int nf = descriptor.length;
						descriptor = Arrays.copyOf(descriptor, nf + 2);
						descriptor[nf] = (kp.x / 125f) - 0.5f;
						descriptor[nf + 1] = (kp.y / 160f) - 0.5f;

						allKeys.add(new FloatDSIFTKeypoint(kp.x, kp.y, descriptor, kp.energy));
					}
				}
				return allKeys;
			}

		});

		FVFWExperiment.main(null);
	}
}
