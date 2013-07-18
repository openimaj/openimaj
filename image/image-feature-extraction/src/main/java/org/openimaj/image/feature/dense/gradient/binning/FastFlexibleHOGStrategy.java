package org.openimaj.image.feature.dense.gradient.binning;

import org.openimaj.image.analysis.algorithm.histogram.WindowedHistogramExtractor;
import org.openimaj.image.analysis.algorithm.histogram.binning.SpatialBinningStrategy;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.statistics.distribution.Histogram;

public class FastFlexibleHOGStrategy implements SpatialBinningStrategy {
	int numCellsX = 8;
	int numCellsY = 16;
	int cellsPerBlockX = 2;
	int cellsPerBlockY = 2;

	private int numBlocksX;
	private int numBlocksY;
	private Histogram[][] blocks;
	private Histogram[][] cells;

	public FastFlexibleHOGStrategy(int numCellsX, int numCellsY, int cellsPerBlock) {
		this(numCellsX, numCellsY, cellsPerBlock, cellsPerBlock);
	}

	public FastFlexibleHOGStrategy(int numCellsX, int numCellsY, int cellsPerBlockX, int cellsPerBlockY) {
		super();
		this.numCellsX = numCellsX;
		this.numCellsY = numCellsY;
		this.cellsPerBlockX = cellsPerBlockX;
		this.cellsPerBlockY = cellsPerBlockY;

		cells = new Histogram[numCellsY][numCellsX];

		numBlocksX = cells[0].length - cellsPerBlockX;
		numBlocksY = cells.length - cellsPerBlockY;
		blocks = new Histogram[numBlocksY][numBlocksX];
	}

	@Override
	public Histogram extract(WindowedHistogramExtractor binnedData, Rectangle region, Histogram output) {
		if (cells[0][0] == null || cells[0][0].values.length != binnedData.getNumBins()) {
			for (int j = 0; j < numCellsY; j++)
				for (int i = 0; i < numCellsX; i++)
					cells[j][i] = new Histogram(binnedData.getNumBins());

			for (int j = 0; j < numBlocksY; j++)
				for (int i = 0; i < numBlocksX; i++)
					blocks[j][i] = new Histogram(binnedData.getNumBins() * cellsPerBlockX * cellsPerBlockY);
		}

		computeCells(binnedData, region);
		computeBlocks(cells);

		final int blockLength = blocks[0][0].values.length;
		final int normBlockLength = blockLength * 4;

		if (output == null || output.values.length != blocks[0].length * blocks.length * normBlockLength)
			output = new Histogram(blocks[0].length * blocks.length * normBlockLength);
		final double[] normBlockData = output.values;

		for (int j = 0, k = 0; j < blocks.length; j++) {
			for (int i = 0; i < blocks[0].length; i++, k++) {
				final double[] block = blocks[j][i].values;

				// each cell is l2 normed, so it follows that the l2 norm of the
				// block is simply the values divided by the area
				final double l2normInv = 0.25;
				double l2clipNorm = 0;
				double l1norm = 0;

				for (int x = 0; x < blockLength; x++) {
					final double val = block[x];

					l1norm += Math.abs(val);

					final double l2 = val * l2normInv;
					final double l2clip = l2 > 0.2 ? 0.2 : l2;
					l2clipNorm += (l2clip * l2clip);

					normBlockData[x + (normBlockLength * k)] = l2;
					normBlockData[x + (normBlockLength * k) + blockLength] = l2clip;
				}

				final double l1normInv = 1.0 / l1norm;
				final double l2clipNormInv = 1.0 / Math.sqrt(l2clipNorm);
				for (int x = 0; x < blockLength; x++) {
					normBlockData[x + (normBlockLength * k) + blockLength] *= l2clipNormInv;

					final double val = block[x];
					final double l1 = val * l1normInv;
					normBlockData[x + (normBlockLength * k) + (2 * blockLength)] = l1;
					normBlockData[x + (normBlockLength * k) + (3 * blockLength)] = Math.sqrt(l1);
				}
			}
		}

		return output;
	}

	private void computeBlocks(Histogram[][] cells) {
		for (int y = 0; y < numBlocksY; y++) {
			for (int x = 0; x < numBlocksX; x++) {
				final double[] blockData = blocks[y][x].values;

				for (int j = 0, k = 0; j < cellsPerBlockY; j++) {
					for (int i = 0; i < cellsPerBlockX; i++) {
						final double[] cellData = cells[y + j][x + i].values;

						System.arraycopy(cellData, 0, blockData, k, cellData.length);

						k += cellData.length;
					}
				}
			}
		}
	}

	private void computeCells(WindowedHistogramExtractor binnedData, Rectangle region) {
		final int cellWidth = (int) (region.width / numCellsX);
		final int cellHeight = (int) (region.height / numCellsY);

		for (int j = 0, y = (int) region.y; j < numCellsY; j++, y += cellHeight) {
			for (int i = 0, x = (int) region.x; i < numCellsX; i++, x += cellWidth) {
				binnedData.computeHistogram(x, y, cellWidth, cellHeight, cells[j][i]);
				cells[j][i].normaliseL2();
			}
		}
	}
}
