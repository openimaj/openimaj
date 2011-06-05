package org.openimaj.image.feature.dense.binarypattern;

import gnu.trove.TIntArrayList;

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
