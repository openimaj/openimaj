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
package org.openimaj.image.feature.local.aggregate;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.feature.ArrayFeatureVector;
import org.openimaj.feature.FeatureVector;
import org.openimaj.feature.local.LocalFeature;
import org.openimaj.feature.local.SpatialLocation;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.concatenate.Concatenatable;

/**
 * A {@link BlockSpatialAggregator} performs spatial pooling of local features
 * by grouping the local features into non-overlapping, fixed-size spatial
 * blocks, and applying a {@link VectorAggregator} (i.e. a
 * {@link BagOfVisualWords}) to the features within each block before combining
 * the aggregated results into a single vector (by passing through the blocks in
 * a left-right, top-bottom fashion).
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <T>
 *            Primitive array type of the backing array of each local feature
 * @param <AGGREGATE>
 *            Type of the aggregate {@link FeatureVector} produced
 */
public class BlockSpatialAggregator<T, AGGREGATE extends FeatureVector & Concatenatable<AGGREGATE, AGGREGATE>>
		implements
		SpatialVectorAggregator<ArrayFeatureVector<T>, SpatialLocation, Rectangle>
{
	protected VectorAggregator<ArrayFeatureVector<T>, AGGREGATE> innerAggregator;
	protected int blocksX;
	protected int blocksY;

	/**
	 * Construct with the given aggregator and number of blocks in the X and Y
	 * dimensions.
	 * 
	 * @param innerAggregator
	 *            the aggregator
	 * @param blocksX
	 *            the number of blocks in X
	 * @param blocksY
	 *            the number of blocks in Y
	 */
	public BlockSpatialAggregator(VectorAggregator<ArrayFeatureVector<T>, AGGREGATE> innerAggregator, int blocksX,
			int blocksY)
	{
		this.innerAggregator = innerAggregator;
		this.blocksX = blocksX;
		this.blocksY = blocksY;
	}

	@Override
	public AGGREGATE aggregate(
			List<? extends LocalFeature<? extends SpatialLocation, ? extends ArrayFeatureVector<T>>> features,
			Rectangle bounds)
	{
		final List<List<LocalFeature<? extends SpatialLocation, ? extends ArrayFeatureVector<T>>>> spatialFeatures =
				new ArrayList<List<LocalFeature<? extends SpatialLocation, ? extends ArrayFeatureVector<T>>>>(blocksX
						* blocksY);

		for (int i = 0; i < blocksX * blocksY; i++)
			spatialFeatures
					.add(new ArrayList<LocalFeature<? extends SpatialLocation, ? extends ArrayFeatureVector<T>>>());

		final float stepX = (bounds.width - bounds.x) / blocksX;
		final float stepY = (bounds.height - bounds.y) / blocksY;

		for (final LocalFeature<? extends SpatialLocation, ? extends ArrayFeatureVector<T>> f : features) {
			final SpatialLocation l = f.getLocation();

			final int xbin = (int) Math.floor((l.x - bounds.x) / stepX);
			final int ybin = (int) Math.floor((l.y - bounds.y) / stepY);
			final int idx = xbin + blocksX * ybin;

			spatialFeatures.get(idx).add(f);
		}

		final AGGREGATE first = innerAggregator.aggregate(spatialFeatures.get(0));

		final List<AGGREGATE> others = new ArrayList<AGGREGATE>(spatialFeatures.size() - 1);
		for (int i = 1; i < spatialFeatures.size(); i++) {
			others.add(innerAggregator.aggregate(spatialFeatures.get(i)));
		}

		return first.concatenate(others);
	}
}
