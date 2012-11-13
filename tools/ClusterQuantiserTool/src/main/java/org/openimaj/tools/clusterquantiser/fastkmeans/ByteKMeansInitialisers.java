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
package org.openimaj.tools.clusterquantiser.fastkmeans;

import java.io.File;
import java.io.IOException;

import org.kohsuke.args4j.CmdLineOptionsProvider;
import org.kohsuke.args4j.Option;
import org.openimaj.data.DataSource;
import org.openimaj.io.IOUtils;
import org.openimaj.ml.clustering.ByteCentroidsResult;
import org.openimaj.ml.clustering.kmeans.ByteKMeans;
import org.openimaj.ml.clustering.kmeans.ByteKMeansInit;

/**
 * Initialisation options for k-means.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public enum ByteKMeansInitialisers implements CmdLineOptionsProvider {
	/**
	 * Randomly sampled points to start
	 */
	RANDOM {
		@Override
		public Options getOptions() {
			return new RandomOptions();
		}
	},
	/**
	 * Start from provided centroids
	 */
	RANDOMSETCLUSTER {
		@Override
		public Options getOptions() {
			return new RandomSetClusterOptions();
		}
	};

	@Override
	public abstract Options getOptions();

	/**
	 * Base options for FastByteKMeansInitialisers types
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * 
	 */
	public abstract class Options {
		/**
		 * Initialise the clusterer
		 * 
		 * @param fkmb
		 * @throws Exception
		 */
		public abstract void setClusterInit(ByteKMeans fkmb) throws Exception;
	}

	class RandomOptions extends Options {
		@Override
		public void setClusterInit(ByteKMeans fkmb) {
			fkmb.setInit(new ByteKMeansInit.RANDOM());
		}
	}

	class RandomSetClusterOptions extends Options {
		@Option(name = "--random-set-source", aliases = "-rss", required = true, usage = "Specify the random set source")
		private File randomSetSource = null;

		@Override
		public void setClusterInit(ByteKMeans fkmb) throws IOException {
			class RANDOMSETINIT extends ByteKMeansInit {
				private File f;

				public RANDOMSETINIT(File f) {
					this.f = f;
				}

				@Override
				public void initKMeans(DataSource<byte[]> bds, byte[][] clusters) throws IOException {
					System.out.println("...Loading RANDOMSET cluster for FASTKMEANS init");
					final ByteCentroidsResult rsbc = IOUtils.read(f, ByteCentroidsResult.class);
					final byte[][] toBeCopied = rsbc.getCentroids();
					for (int i = 0; i < clusters.length; i++) {
						// If the random set cluster is too small for this
						// cluster, pad the remaining space with random entries
						// from the data source
						if (i > toBeCopied.length) {
							final int remaining = clusters.length - i;
							final byte[][] padding = new byte[remaining][bds.numDimensions()];
							bds.getRandomRows(padding);
							for (int j = 0; j < padding.length; j++) {
								System.arraycopy(padding[j], 0, clusters[i + j], 0, padding[j].length);
							}
							break;
						}

						System.arraycopy(toBeCopied[i], 0, clusters[i], 0, toBeCopied[i].length);
					}
					System.out.println("...Done");
				}
			}

			fkmb.setInit(new RANDOMSETINIT(randomSetSource));
		}
	}
}
