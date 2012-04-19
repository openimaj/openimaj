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

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.math.statistics.distribution.MultidimensionalHistogram;


/**
 * An array of multidimensional histograms calculated from image pixels using a mask
 * (assumes image is in 0-1 range)
 * 
 * @author Jonathon Hare
 *
 */
public class MaskingBlockHistogramModel extends BlockHistogramModel {
	private static final long serialVersionUID = 1L;
	private FImage mask;
	
	/**
	 * Construct with the given parameters
	 * @param mask the mask image
	 * @param blocks_x the number of blocks in the x-direction
	 * @param blocks_y the number of blocks in the y-direction
	 * @param nbins the number of bins in each dimension for the histograms
	 */
	public MaskingBlockHistogramModel(FImage mask, int blocks_x, int blocks_y, int... nbins) {
		super(blocks_x, blocks_y, nbins);
		
		this.mask = mask;
	}
	
	@Override
	protected void accum(MBFImage im, int bx, int by) {
		assert (im.numBands() == ndims);

		MultidimensionalHistogram histogram = histograms[by][bx];
		int height = im.getHeight();
		int width = im.getWidth();
		
		int cols_per_block = width / blocks_x;		
		int startx = bx*cols_per_block;
		int stopx = (1+bx)*cols_per_block;

		int rows_per_block = height / blocks_y;
		int starty = by*rows_per_block;
		int stopy = (1+by)*rows_per_block;
		
		if (stopx >= width) stopx = width;
		if (stopy >= height) stopy = height;
		
		for (int y=starty; y<stopy; y++) {
			for (int x=startx; x<stopx; x++) {
				if (mask.pixels[y][x] != 1)
					continue;
				
				int [] bins = new int[ndims];
				
				for (int i=0; i<ndims; i++) {
					bins[i] = (int)(im.getBand(i).pixels[y][x] * (histogram.nbins[i]));
					if (bins[i] >= histogram.nbins[i]) bins[i] = histogram.nbins[i] - 1;
				}
				
				int bin = 0;
				for (int i=0; i<ndims; i++) {
					int f = 1;
					for (int j=0; j<i; j++)
						f *= histogram.nbins[j];
					
					bin += f * bins[i];
				}
				
				histogram.values[bin]++;
			}
		}
	}
}
