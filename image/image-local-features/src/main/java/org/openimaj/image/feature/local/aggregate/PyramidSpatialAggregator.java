package org.openimaj.image.feature.local.aggregate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openimaj.feature.ArrayFeatureVector;
import org.openimaj.feature.FeatureVector;
import org.openimaj.feature.local.LocalFeature;
import org.openimaj.feature.local.SpatialLocation;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.concatenate.Concatenatable;

public class PyramidSpatialAggregator<T, AGGREGATE extends FeatureVector & Concatenatable<AGGREGATE, AGGREGATE>>
		implements
		SpatialVectorAggregator<ArrayFeatureVector<T>, SpatialLocation, Rectangle>
{
	protected VectorAggregator<ArrayFeatureVector<T>, AGGREGATE> innerAggregator;
	boolean[][][] levels;

	public PyramidSpatialAggregator(VectorAggregator<ArrayFeatureVector<T>, AGGREGATE> innerAggregator, String description)
	{
		this.innerAggregator = innerAggregator;
		this.levels = parseLevelsSimple(description);
	}

	public PyramidSpatialAggregator(VectorAggregator<ArrayFeatureVector<T>, AGGREGATE> innerAggregator, int... numBlocks)
	{
		this.innerAggregator = innerAggregator;
		this.levels = new boolean[numBlocks.length][][];

		for (int i = 0; i < numBlocks.length; i++) {
			this.levels[i] = new boolean[numBlocks[i]][numBlocks[i]];

			for (int j = 0; j < numBlocks[i]; j++)
				Arrays.fill(levels[i][j], true);
		}
	}

	private static boolean[][][] parseLevelsSimple(String simple) {
		final String[] parts = simple.split("-");
		final boolean[][][] levels = new boolean[parts.length][][];

		for (int i = 0; i < parts.length; i++) {
			final String part = parts[i];
			final String[] tmp = part.split("x");

			if (tmp.length != 2)
				throw new IllegalArgumentException("Invalid specification string");

			final int binsX = Integer.parseInt(tmp[0]);
			final int binsY = Integer.parseInt(tmp[1]);

			levels[i] = new boolean[binsY][binsX];

			for (int j = 0; j < binsY; j++)
				Arrays.fill(levels[i][j], true);
		}

		return levels;
	}

	private static boolean[][][] parseLevelsAdvanced(String description) {
		final String[] parts = description.split("[+]");
		final List<boolean[][]> levels = new ArrayList<boolean[][]>();

		for (int i = 0; i < parts.length; i++) {
			final List<String> levelDesc = new ArrayList<String>();

			levelDesc.add(parts[i]);
			final String key = parts[i].substring(0, parts[i].indexOf("#") + 1);

			for (; i < parts.length; i++) {
				if (parts[i].startsWith(key)) {
					levelDesc.add(parts[i]);
				} else {
					i--;
					break;
				}
			}

			final int x = Integer.parseInt(key.substring(1, key.indexOf("x")));
			final int y = Integer.parseInt(key.substring(key.indexOf("x") + 1, key.indexOf("#")));
			final boolean[][] level = new boolean[y][x];

			for (int yy = 0; yy < y; yy++) {
				for (int xx = 0; xx < x; xx++) {
					final String curr = key + (xx + yy * x);
					if (levelDesc.contains(curr)) {
						level[yy][xx] = true;
					} else {
						level[yy][xx] = false;
					}
				}
			}

			levels.add(level);
		}

		return levels.toArray(new boolean[levels.size()][][]);
	}

	private static String levelsToString(boolean[][][] levels) {
		final StringBuilder sb = new StringBuilder();

		for (int i = 0; i < levels.length; i++) {
			final int y = levels[i].length;
			final int x = levels[i][0].length;
			sb.append("Level " + i + " (" + x + "x" + y + "):\n");

			for (int yy = 0; yy < y; yy++) {
				for (int xx = 0; xx < x; xx++) {
					sb.append(levels[i][yy][xx] ? "X" : "-");
				}
				sb.append("\n");
			}
			sb.append("\n");
		}

		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	@Override
	public AGGREGATE aggregate(
			List<? extends LocalFeature<? extends SpatialLocation, ? extends ArrayFeatureVector<T>>> features,
			Rectangle bounds)
	{
		final List<AGGREGATE> levelFeatures = new ArrayList<AGGREGATE>(levels.length);

		for (int l = 0; l < levels.length; l++) {
			final boolean[][] level = levels[l];
			final int blocksX = level[0].length;
			final int blocksY = level.length;

			final Object[][] spatialData = new Object[blocksY][blocksX];
			for (int y = 0; y < blocksY; y++) {
				for (int x = 0; x < blocksX; x++) {
					if (level[y][x])
						spatialData[y][x] = new ArrayList<LocalFeature<? extends SpatialLocation, ? extends ArrayFeatureVector<T>>>();
				}
			}

			final float stepX = (bounds.width - bounds.x) / blocksX;
			final float stepY = (bounds.height - bounds.y) / blocksY;

			for (final LocalFeature<? extends SpatialLocation, ? extends ArrayFeatureVector<T>> f : features) {
				final SpatialLocation spatialLoc = f.getLocation();

				final int xbin = (int) Math.floor((spatialLoc.x - bounds.x) / stepX);
				final int ybin = (int) Math.floor((spatialLoc.y - bounds.y) / stepY);

				if (level[ybin][xbin]) {
					((List<LocalFeature<? extends SpatialLocation, ? extends ArrayFeatureVector<T>>>) spatialData[ybin][xbin])
							.add(f);
				}
			}

			final List<AGGREGATE> spatialFeatures = new ArrayList<AGGREGATE>(blocksX * blocksY);
			for (int y = 0; y < blocksY; y++) {
				for (int x = 0; x < blocksX; x++) {
					if (spatialData[y][x] != null) {
						final AGGREGATE fv = innerAggregator
								.aggregate((List<LocalFeature<? extends SpatialLocation, ? extends ArrayFeatureVector<T>>>) spatialData[y][x]);

						spatialFeatures.add(fv);
					}
				}
			}
			levelFeatures.add(join(spatialFeatures));
		}

		return join(levelFeatures);
	}

	private AGGREGATE join(List<AGGREGATE> fvs) {
		final AGGREGATE first = fvs.get(0);
		final List<AGGREGATE> others = new ArrayList<AGGREGATE>(fvs.size() - 1);
		for (int i = 1; i < fvs.size(); i++) {
			others.add(fvs.get(i));
		}

		return first.concatenate(others);
	}
}
