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
package org.openimaj.image.pixel.statistics;

import org.openimaj.feature.FeatureVectorProvider;
import org.openimaj.image.MBFImage;
import org.openimaj.math.statistics.distribution.MultidimensionalHistogram;
import org.openimaj.util.pair.Pair;

/**
 * A multidimensional histogram calculated from image pixels (assumes image is
 * in 0-1 range)
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class HistogramModel extends AbstractPixelStatisticsModel
		implements
		FeatureVectorProvider<MultidimensionalHistogram>
{
	private static final long serialVersionUID = 1L;

	/**
	 * The histogram data
	 */
	public MultidimensionalHistogram histogram;

	/**
	 * Construct with the given number of bins per dimension
	 * 
	 * @param nbins
	 *            the number of bins in each dimension for the histograms
	 */
	public HistogramModel(int... nbins) {
		super(nbins.length);

		assert (nbins.length > 0);

		histogram = new MultidimensionalHistogram(nbins);
	}

	@Override
	public void estimateModel(MBFImage... images) {
		reset();
		for (final MBFImage im : images) {
			accum(im);
		}
		histogram.normalise();
	}

	protected void reset() {
		for (int i = 0; i < histogram.values.length; i++)
			histogram.values[i] = 0;
	}

	/**
	 * For a given index, map to the range of colours which could map to it
	 * 
	 * @param index
	 * @return start/end colour
	 */
	public Pair<float[]> colourRange(int index) {
		final int[] coord = this.histogram.getCoordinates(index);
		final float[] start = new float[coord.length];
		final float[] end = new float[coord.length];
		final int[] nbins = histogram.nbins;
		for (int i = 0; i < coord.length; i++) {
			start[i] = (float) coord[i] / (float) nbins[i];
			end[i] = ((float) coord[i] + 1) / nbins[i];
		}
		return new Pair<float[]>(start, end);
	}

	/**
	 * For a given index, get the average colour which would map to it
	 * 
	 * @param index
	 * @return start/end colour
	 */
	public float[] colourAverage(int index) {
		final int[] coord = this.histogram.getCoordinates(index);
		final float[] average = new float[coord.length];
		final int[] nbins = histogram.nbins;
		for (int i = 0; i < coord.length; i++) {
			final float start = (float) coord[i] / (float) nbins[i];
			final float end = ((float) coord[i] + 1) / nbins[i];
			average[i] = (start + end) / 2f;
		}

		return average;
	}

	protected void accum(MBFImage im) {
		final int height = im.getHeight();
		final int width = im.getWidth();
		final int[] bins = new int[ndims];

		final float[][][] bands = new float[im.numBands()][][];
		for (int i = 0; i < bands.length; i++)
			bands[i] = im.getBand(i).pixels;

		final int[] nbins = histogram.nbins;
		final double[] values = histogram.values;

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				for (int i = 0; i < ndims; i++) {
					bins[i] = (int) (bands[i][y][x] * (nbins[i]));
					if (bins[i] >= nbins[i])
						bins[i] = nbins[i] - 1;
				}

				int bin = 0;
				for (int i = 0; i < ndims; i++) {
					int f = 1;
					for (int j = 0; j < i; j++)
						f *= nbins[j];

					bin += f * bins[i];
				}

				values[bin]++;
			}
		}
	}

	@Override
	public String toString() {
		return histogram.toString();
	}

	@Override
	public HistogramModel clone() {
		final HistogramModel model = new HistogramModel();
		model.histogram = histogram.clone();
		model.ndims = ndims;
		return model;
	}

	@Override
	public MultidimensionalHistogram getFeatureVector() {
		return histogram;
	}
}
