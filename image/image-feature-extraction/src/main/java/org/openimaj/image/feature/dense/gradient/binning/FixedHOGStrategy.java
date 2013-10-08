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
package org.openimaj.image.feature.dense.gradient.binning;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.image.analysis.algorithm.histogram.WindowedHistogramExtractor;
import org.openimaj.image.analysis.algorithm.histogram.binning.SpatialBinningStrategy;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.statistics.distribution.Histogram;
import org.openimaj.util.array.ArrayUtils;

/**
 * Implementation of an {@link SpatialBinningStrategy} that extracts normalised
 * HOG features in the style defined by Dalal and Triggs. In this scheme,
 * histograms are computed for fixed size cells (size in pixels), and are
 * aggregated over fixed size blocks of cells. Blocks may overlap. The
 * aggregated histogram for each block is then normalised before being appended
 * into the final output histogram.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Reference(
		type = ReferenceType.Inproceedings,
		author = { "Dalal, Navneet", "Triggs, Bill" },
		title = "Histograms of Oriented Gradients for Human Detection",
		year = "2005",
		booktitle = "Proceedings of the 2005 IEEE Computer Society Conference on Computer Vision and Pattern Recognition (CVPR'05) - Volume 1 - Volume 01",
		pages = { "886", "", "893" },
		url = "http://dx.doi.org/10.1109/CVPR.2005.177",
		publisher = "IEEE Computer Society",
		series = "CVPR '05",
		customData = {
				"isbn", "0-7695-2372-2",
				"numpages", "8",
				"doi", "10.1109/CVPR.2005.177",
				"acmid", "1069007",
				"address", "Washington, DC, USA"
		})
public class FixedHOGStrategy implements SpatialBinningStrategy {
	/**
	 * Block normalisation schemes
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * 
	 */
	public enum BlockNormalisation {
		/**
		 * L1 normalisation
		 */
		L1 {
			@Override
			final void normalise(Histogram h, int blockArea) {
				h.normaliseL1();
			}
		},
		/**
		 * L2 normalisation
		 */
		L2 {
			@Override
			final void normalise(Histogram h, int blockArea) {
				// each cell is l2 normed, so it follows that the l2 norm of the
				// block is simply the values divided by the area
				ArrayUtils.divide(h.values, blockArea);
			}
		},
		/**
		 * L1 normalisation followed by element-wise sqrt
		 */
		L1sqrt {
			@Override
			final void normalise(Histogram h, int blockArea) {
				h.normaliseL1();

				for (int x = 0; x < h.values.length; x++)
					h.values[x] = Math.sqrt(h.values[x]);
			}
		},
		/**
		 * L2 normalisation; clipping values above 0.2; L2 normalisation
		 */
		L2clip {
			@Override
			final void normalise(Histogram h, int blockArea) {
				// each cell is l2 normed, so it follows that the l2 norm of the
				// block is simply the values divided by the area
				double sumsq = 0;
				for (int x = 0; x < h.values.length; x++) {
					h.values[x] = h.values[x] / blockArea;
					if (h.values[x] > 0.2)
						h.values[x] = 0.2;
					sumsq += h.values[x] * h.values[x];
				}

				final double invNorm = 1.0 / Math.sqrt(sumsq);
				for (int x = 0; x < h.values.length; x++) {
					h.values[x] *= invNorm;
				}
			}
		};

		abstract void normalise(Histogram h, int blockArea);
	}

	int cellWidth = 6;
	int cellHeight = 6;
	int cellsPerBlockX = 3;
	int cellsPerBlockY = 3;
	int blockStepX = 1;
	int blockStepY = 1;
	BlockNormalisation norm = BlockNormalisation.L2;

	/**
	 * Construct with square cells of the given size (in pixels). Square blocks
	 * are constructed from the given number of cells in each dimension. Blocks
	 * overlap, and shift by 1 cell (i.e. overlap is cellsPerBlock - 1).
	 * 
	 * @param cellSize
	 *            the size of the cells in pixels
	 * @param cellsPerBlock
	 *            the number of cells per block
	 * @param norm
	 *            the normalisation scheme
	 */
	public FixedHOGStrategy(int cellSize, int cellsPerBlock, BlockNormalisation norm) {
		this(cellSize, cellsPerBlock, 1, norm);
	}

	/**
	 * Construct with square cells of the given size (in pixels). Square blocks
	 * are constructed from the given number of cells in each dimension.
	 * 
	 * @param cellSize
	 *            the size of the cells in pixels
	 * @param cellsPerBlock
	 *            the number of cells per block
	 * @param blockStep
	 *            the amount to shift each block in terms of cells
	 * @param norm
	 *            the normalisation scheme
	 */
	public FixedHOGStrategy(int cellSize, int cellsPerBlock, int blockStep, BlockNormalisation norm) {
		this(cellSize, cellSize, cellsPerBlock, cellsPerBlock, blockStep, blockStep, norm);
	}

	/**
	 * Construct with square cells of the given size (in pixels). Square blocks
	 * are constructed from the given number of cells in each dimension.
	 * 
	 * @param cellWidth
	 *            the width of the cells in pixels
	 * @param cellHeight
	 *            the height of the cells in pixels
	 * @param cellsPerBlockX
	 *            the number of cells per block in the x direction
	 * @param cellsPerBlockY
	 *            the number of cells per block in the y direction
	 * @param blockStepX
	 *            the amount to shift each block in terms of cells in the x
	 *            direction
	 * @param blockStepY
	 *            the amount to shift each block in terms of cells in the y
	 *            direction
	 * @param norm
	 *            the normalisation scheme
	 */
	public FixedHOGStrategy(int cellWidth, int cellHeight, int cellsPerBlockX, int cellsPerBlockY, int blockStepX,
			int blockStepY, BlockNormalisation norm)
	{
		super();
		this.cellWidth = cellWidth;
		this.cellHeight = cellHeight;
		this.cellsPerBlockX = cellsPerBlockX;
		this.cellsPerBlockY = cellsPerBlockY;
		this.norm = norm;
		this.blockStepX = blockStepX;
		this.blockStepY = blockStepY;
	}

	@Override
	public Histogram extract(WindowedHistogramExtractor binnedData, Rectangle region, Histogram output) {
		final Histogram[][] cells = computeCells(binnedData, region);
		final Histogram[][] blocks = computeBlocks(cells);

		final int blockSize = blocks[0][0].values.length;
		final int blockArea = cellsPerBlockX * cellsPerBlockY;

		if (output == null || output.values.length != blocks[0].length * blocks.length * blockSize)
			output = new Histogram(blocks[0].length * blocks.length * blockSize);

		for (int j = 0, k = 0; j < blocks.length; j++) {
			for (int i = 0; i < blocks[0].length; i++, k++) {
				norm.normalise(blocks[j][i], blockArea);

				System.arraycopy(blocks[j][i].values, 0, output.values, k * blockSize, blockSize);
			}
		}

		return output;
	}

	private Histogram[][] computeBlocks(Histogram[][] cells) {
		final int numBlocksX = 1 + (cells[0].length - cellsPerBlockX) / this.blockStepX;
		final int numBlocksY = 1 + (cells.length - cellsPerBlockY) / this.blockStepY;
		final Histogram[][] blocks = new Histogram[numBlocksY][numBlocksX];

		for (int y = 0; y < numBlocksY; y++) {
			for (int x = 0; x < numBlocksX; x++) {
				final Histogram[] blockData = new Histogram[cellsPerBlockX * cellsPerBlockY];

				for (int j = 0, k = 0; j < cellsPerBlockY; j++) {
					for (int i = 0; i < cellsPerBlockX; i++) {
						blockData[k++] = cells[y * blockStepY + j][x * blockStepX + i];
					}
				}

				blocks[y][x] = new Histogram(blockData);
			}
		}
		return blocks;
	}

	private Histogram[][] computeCells(WindowedHistogramExtractor binnedData, Rectangle region) {
		final int numCellsX = (int) ((region.width + cellWidth / 2) / cellWidth);
		final int numCellsY = (int) ((region.height + cellHeight / 2) / cellHeight);

		final Histogram[][] cells = new Histogram[numCellsY][numCellsX];
		for (int j = 0, y = (int) region.y; j < numCellsY; j++, y += cellHeight) {
			for (int i = 0, x = (int) region.x; i < numCellsX; i++, x += cellWidth) {
				cells[j][i] = binnedData.computeHistogram(x, y, cellWidth, cellHeight);
				cells[j][i].normaliseL2();
			}
		}

		return cells;
	}
}
