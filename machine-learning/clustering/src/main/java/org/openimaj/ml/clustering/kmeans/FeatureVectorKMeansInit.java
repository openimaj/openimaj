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
package org.openimaj.ml.clustering.kmeans;

import java.io.IOException;

import org.openimaj.data.DataSource;

import com.rits.cloning.Cloner;

/**
 * Initialisation for K-Means clustering. Given a data source of samples and a
 * set of clusters to fill, implementations of this class should initialise the
 * KMeans algorithm.
 *
 * A default RANDOM implementation is provided which uses
 * {@link DataSource#getRandomRows}
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <T>
 *            Type of object being clustered
 */
public abstract class FeatureVectorKMeansInit<T> {
	/**
	 * Initialise the centroids based on the given data.
	 *
	 * @param bds
	 *            the data source of samples
	 * @param clusters
	 *            the clusters to init
	 * @throws IOException
	 *             problem reading samples
	 */
	public abstract void initKMeans(DataSource<T> bds, T[] clusters) throws IOException;

	/**
	 * Simple kmeans initialized on randomly selected samples.
	 *
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 * @param <T>
	 *            Type of object being clustered
	 */
	public static class RANDOM<T> extends FeatureVectorKMeansInit<T> {
		@Override
		public void initKMeans(DataSource<T> bds, T[] clusters) throws IOException {
			bds.getRandomRows(clusters);

			final Cloner cloner = new Cloner();
			for (int i = 0; i < clusters.length; i++) {
				clusters[i] = cloner.deepClone(clusters[i]);
			}
		}
	}
}
