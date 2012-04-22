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
package org.openimaj.image.feature.dense.binarypattern;

import gnu.trove.list.array.TIntArrayList;

import org.openimaj.feature.FloatFV;

public class LocalUniformBinaryPatternHistogram {
	protected int blocksize_x;
	protected int blocksize_y;
	FloatFV[][] histograms;
	
	public LocalUniformBinaryPatternHistogram(int blocksize_x, int blocksize_y) {
		this.blocksize_x = blocksize_x;
		this.blocksize_y = blocksize_y;
	}

	public void calculateHistograms(int[][] patternImage, int nbits) {
		int height = patternImage.length;
		int width = patternImage[0].length;
		TIntArrayList uniformPatterns = UniformBinaryPattern.getUniformPatterns(nbits);
		
		histograms = new FloatFV[(int) Math.ceil((double)height/(double)blocksize_y)][(int) Math.ceil((double)width/(double)blocksize_x)];
		
		for (int y=0, j=0; y<height; y+=blocksize_y, j++) {
			for (int x=0, i=0; x<width; x+=blocksize_x, i++) {
				histograms[j][i] = new FloatFV(uniformPatterns.size() + 1);
				
				for (int yy=y; yy<Math.min(height, y+blocksize_y); yy++) {
					for (int xx=x; xx<Math.min(width, x+blocksize_x); xx++) {
						int idx = uniformPatterns.indexOf(patternImage[yy][xx]);
						
						histograms[j][i].values[idx + 1]++;
					}
				}
			}
		}
	}
	
	public FloatFV[][] getHistograms() {
		return histograms;
	}
	
	public FloatFV getHistogram() {
		int len = histograms[0][0].length();
		FloatFV h = new FloatFV(histograms.length * histograms[0].length * len);

		for (int j=0; j<histograms.length; j++) {
			for (int i=0; i<histograms[0].length; i++) {
				int blkid = i + j*histograms[0].length;
				System.arraycopy(histograms[j][i].values, 0, h.values, blkid*len, len);
			}
		}
		
		return h;
	}
}
