package org.openimaj.image.feature.dense.gradient.binning;

import org.openimaj.feature.DoubleFV;
import org.openimaj.image.FImage;
import org.openimaj.image.analysis.algorithm.BinnedImageHistogramAnalyser;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.statistics.distribution.Histogram;
import org.openimaj.util.array.ArrayUtils;

public class BasicHOGStrategy implements SpatialBinningStrategy {
	int cellWidth = 6;
	int cellHeight = 6;
	int cellsPerBlockX = 3;
	int cellsPerBlockY = 3;

	@Override
	public Histogram extract(BinnedImageHistogramAnalyser binnedData, FImage magnitudes, Rectangle region) {
		final Histogram[][] cells = computeCells(binnedData, magnitudes, region);
		final Histogram[][] blocks = computeBlocks(cells);

		// final Histogram[] normBlocks = new Histogram[blocks[0].length *
		// blocks.length * 4];
		final Histogram[] normBlocks = new Histogram[blocks[0].length * blocks.length];

		for (int j = 0, k = 0; j < blocks.length; j++) {
			for (int i = 0; i < blocks[0].length; i++) {
				final DoubleFV l1 = blocks[j][i].normaliseFV(1);
				final DoubleFV l2 = blocks[j][i].normaliseFV(2);

				final double[] l1sqrt = l1.values.clone();
				for (int x = 0; x < l1sqrt.length; x++)
					l1sqrt[x] = Math.sqrt(l1sqrt[x]);

				final double[] l2clip = l2.values.clone();
				for (int x = 0; x < l1sqrt.length; x++)
					l1sqrt[x] = l1sqrt[x] > 0.2 ? 0.2 : l1sqrt[x];
				ArrayUtils.normalise(l2clip);

				// normBlocks[k++] = new Histogram(l2);
				// normBlocks[k++] = new Histogram(l2clip);
				// normBlocks[k++] = new Histogram(l1);
				// normBlocks[k++] = new Histogram(l1sqrt);

				for (int x = 0; x < l1sqrt.length; x++)
					l2.values[x] = (l2.values[x] + l2clip[x] + l1.values[x] + l1sqrt[x]) / 4.0;
				normBlocks[k++] = new Histogram(l2);
			}
		}

		return new Histogram(normBlocks);
	}

	private Histogram[][] computeBlocks(Histogram[][] cells) {
		final int numBlocksX = cells[0].length - cellsPerBlockX;
		final int numBlocksY = cells.length - cellsPerBlockY;
		final Histogram[][] blocks = new Histogram[numBlocksY][numBlocksX];

		for (int y = 0; y < numBlocksY; y++) {
			for (int x = 0; x < numBlocksX; x++) {
				final Histogram[] blockData = new Histogram[cellsPerBlockX * cellsPerBlockY];
				for (int j = 0, k = 0; j < cellsPerBlockY; j++) {
					for (int i = 0; i < cellsPerBlockX; i++) {
						blockData[k++] = cells[y + j][x + i];
					}
				}

				blocks[y][x] = new Histogram(blockData);
			}
		}
		return blocks;
	}

	private Histogram[][] computeCells(BinnedImageHistogramAnalyser binnedData, FImage magnitudes, Rectangle region) {
		// TODO: bilerp
		final int numCellsX = (int) ((region.width + cellWidth / 2) / cellWidth);
		final int numCellsY = (int) ((region.height + cellHeight / 2) / cellHeight);

		final int[][] map = binnedData.getBinMap();
		final float[][] weights = magnitudes.pixels;
		final int nbins = binnedData.getNumBins();

		final Histogram[][] cells = new Histogram[numCellsY][numCellsX];
		for (int j = 0; j < numCellsY; j++)
			for (int i = 0; i < numCellsX; i++)
				cells[j][i] = new Histogram(nbins);

		for (int y = 0; y < region.height; y++) {
			final int cellY = y / cellHeight;

			if (cellY >= numCellsY)
				break;

			for (int x = 0; x < region.width; x++) {
				final int cellX = x / cellWidth;

				if (cellX >= numCellsX)
					break;

				final int xx = (int) (region.x + x);
				final int yy = (int) (region.y + y);

				if (xx >= 0 && yy >= 0 && xx < magnitudes.width && yy < magnitudes.height)
					cells[cellY][cellX].values[map[yy][xx]] += weights[yy][xx];
			}
		}

		for (int j = 0; j < numCellsY; j++)
			for (int i = 0; i < numCellsX; i++)
				cells[j][i].normalise();

		return cells;
	}
}
