package org.openimaj.image.feature.dense.gradient.binning;

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

	public BasicHOGStrategy(int cellSize, int cellsPerBlock) {
		this(cellSize, cellSize, cellsPerBlock, cellsPerBlock);
	}

	public BasicHOGStrategy(int cellWidth, int cellHeight, int cellsPerBlockX, int cellsPerBlockY) {
		super();
		this.cellWidth = cellWidth;
		this.cellHeight = cellHeight;
		this.cellsPerBlockX = cellsPerBlockX;
		this.cellsPerBlockY = cellsPerBlockY;
	}

	@Override
	public Histogram extract(BinnedImageHistogramAnalyser binnedData, FImage magnitudes, Rectangle region) {
		final Histogram[][] cells = computeCells(binnedData, magnitudes, region);
		final Histogram[][] blocks = computeBlocks(cells);

		final Histogram[] normBlocks = new Histogram[blocks[0].length * blocks.length * 4];
		// final Histogram[] normBlocks = new Histogram[blocks[0].length *
		// blocks.length];

		for (int j = 0, k = 0; j < blocks.length; j++) {
			for (int i = 0; i < blocks[0].length; i++) {
				final Histogram l1 = blocks[j][i].clone();
				l1.normaliseL1();

				// each cell is l2 normed, so it follows that the l2 norm of the
				// block is simply the values divided by the area
				final Histogram l2 = blocks[j][i].clone();
				ArrayUtils.divide(l2.values, cellsPerBlockX * cellsPerBlockY);

				final Histogram l1sqrt = l1.clone();
				for (int x = 0; x < l1sqrt.values.length; x++)
					l1sqrt.values[x] = Math.sqrt(l1sqrt.values[x]);

				final Histogram l2clip = l2.clone();
				double sumsq = 0;
				for (int x = 0; x < l2clip.values.length; x++) {
					l2clip.values[x] = l2clip.values[x] > 0.2 ? 0.2 : l2clip.values[x];
					sumsq += l2clip.values[x];
				}
				ArrayUtils.divide(l2clip.values, Math.sqrt(sumsq));

				normBlocks[k++] = l2;
				normBlocks[k++] = l2clip;
				normBlocks[k++] = l1;
				normBlocks[k++] = l1sqrt;

				// for (int x = 0; x < l1sqrt.length; x++)
				// l2.values[x] = (l2.values[x] + l2clip[x] + l1.values[x] +
				// l1sqrt[x]) / 4.0;
				// normBlocks[k++] = new Histogram(l2);
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
				cells[j][i].normaliseL2();

		return cells;
	}
}
