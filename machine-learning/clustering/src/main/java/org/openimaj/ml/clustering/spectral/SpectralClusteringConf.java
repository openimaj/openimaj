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
package org.openimaj.ml.clustering.spectral;

import org.openimaj.ml.clustering.SpatialClusterer;
import org.openimaj.ml.clustering.SpatialClusters;
import org.openimaj.util.function.Function;
import org.openimaj.util.pair.IndependentPair;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @param <DATATYPE>
 *
 */
public class SpectralClusteringConf<DATATYPE> {
	/**
	 * A function which can represent itself as a string
	 *
	 * @param <DATATYPE>
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 */
	public static interface ClustererProvider<DATATYPE>
			extends
				Function<IndependentPair<double[], double[][]>, SpatialClusterer<? extends SpatialClusters<DATATYPE>, DATATYPE>>
	{
		@Override
		public String toString();
	}

	protected static class DefaultClustererFunction<DATATYPE> implements ClustererProvider<DATATYPE> {

		private SpatialClusterer<? extends SpatialClusters<DATATYPE>, DATATYPE> internal;

		public DefaultClustererFunction(SpatialClusterer<? extends SpatialClusters<DATATYPE>, DATATYPE> internal) {
			this.internal = internal;
		}

		@Override
		public SpatialClusterer<? extends SpatialClusters<DATATYPE>, DATATYPE> apply(
				IndependentPair<double[], double[][]> in)
		{
			return internal;
		}

		@Override
		public String toString() {
			return internal.toString();
		}

	}

	/**
	 * The internal clusterer
	 */

	ClustererProvider<DATATYPE> internal;

	/**
	 * The graph laplacian creator
	 */
	public GraphLaplacian laplacian;

	/**
	 * The method used to select the number of eigen vectors from the lower
	 * valued eigenvalues
	 */
	public EigenChooser eigenChooser;

	/**
	 *
	 */
	public int skipEigenVectors = 0;

	/**
	 *
	 */
	public boolean eigenValueScale = false;

	/**
	 * @param internal
	 *            the internal clusterer
	 * @param eigK
	 *            the value handed to {@link HardCodedEigenChooser}
	 *
	 */
	public SpectralClusteringConf(SpatialClusterer<? extends SpatialClusters<DATATYPE>, DATATYPE> internal, int eigK) {
		this.internal = new DefaultClustererFunction<DATATYPE>(internal);
		this.laplacian = new GraphLaplacian.Normalised();
		this.eigenChooser = new HardCodedEigenChooser(eigK);

	}

	/**
	 * The underlying {@link EigenChooser} is set to an
	 * {@link ChangeDetectingEigenChooser} which looks for a 100x gap between
	 * eigen vectors to select number of clusters. It also insists upon a
	 * maximum of 0.1 * number of data items (so 10 items per cluster)
	 *
	 * @param internal
	 *            the internal clusterer
	 *
	 */
	public SpectralClusteringConf(SpatialClusterer<? extends SpatialClusters<DATATYPE>, DATATYPE> internal) {
		this.internal = new DefaultClustererFunction<DATATYPE>(internal);
		this.laplacian = new GraphLaplacian.Normalised();
		this.eigenChooser = new ChangeDetectingEigenChooser(100, 0.1);

	}

	/**
	 * @param internal
	 *            an internal clusterer
	 * @param lap
	 *            the laplacian
	 * @param top
	 *            the top eigen vectors
	 */
	public SpectralClusteringConf(SpatialClusterer<? extends SpatialClusters<DATATYPE>, DATATYPE> internal,
			GraphLaplacian lap, int top)
	{
		this.internal = new DefaultClustererFunction<DATATYPE>(internal);
		this.laplacian = lap;
		this.eigenChooser = new HardCodedEigenChooser(top);
	}

	/**
	 * The underlying {@link EigenChooser} is set to an
	 * {@link ChangeDetectingEigenChooser} which looks for a 100x gap between
	 * eigen vectors to select number of clusters. It also insists upon a
	 * maximum of 0.1 * number of data items (so 10 items per cluster)
	 *
	 * @param internal
	 *            the internal clusterer
	 * @param laplacian
	 *            the graph laplacian
	 *
	 */
	public SpectralClusteringConf(SpatialClusterer<? extends SpatialClusters<DATATYPE>, DATATYPE> internal,
			GraphLaplacian laplacian)
	{
		this.internal = new DefaultClustererFunction<DATATYPE>(internal);
		this.laplacian = laplacian;
		this.eigenChooser = new ChangeDetectingEigenChooser(100, 0.1);

	}

	/**
	 * The underlying {@link EigenChooser} is set to an
	 * {@link ChangeDetectingEigenChooser} which looks for a 100x gap between
	 * eigen vectors to select number of clusters. It also insists upon a
	 * maximum of 0.1 * number of data items (so 10 items per cluster)
	 * 
	 * @param internal
	 *            the internal clusterer
	 *
	 */
	public SpectralClusteringConf(ClustererProvider<DATATYPE> internal) {
		this.internal = internal;
		this.laplacian = new GraphLaplacian.Normalised();
		this.eigenChooser = new ChangeDetectingEigenChooser(100, 0.1);

	}

}
