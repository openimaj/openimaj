package org.openimaj.image.feature.local.aggregate;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.feature.ArrayFeatureVector;
import org.openimaj.feature.FeatureVector;
import org.openimaj.feature.local.LocalFeature;
import org.openimaj.feature.local.SpatialLocation;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.concatenate.Concatenatable;

public class BlockSpatialAggregator<T, AGGREGATE extends FeatureVector & Concatenatable<AGGREGATE, AGGREGATE>>
		implements
		SpatialVectorAggregator<ArrayFeatureVector<T>, SpatialLocation, Rectangle>
{
	protected VectorAggregator<ArrayFeatureVector<T>, AGGREGATE> innerAggregator;
	protected int blocksX;
	protected int blocksY;

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
