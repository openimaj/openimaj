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
package org.openimaj.image.saliency;

import java.util.Arrays;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.image.FImage;
import org.openimaj.image.processing.convolution.AverageBoxFilter;
import org.openimaj.image.processing.convolution.FConvolution;

/**
 * Construct a map that shows the "focus" of each pixel. 
 * A value of 0 in the output corresponds to a sharp pixel, whilst higher
 * values correspond to more blurred pixels.
 * 
 * Algorithm based on:
 * Yiwen Luo and Xiaoou Tang. 2008. 
 * Photo and Video Quality Evaluation: Focusing on the Subject. 
 * In Proceedings of the 10th European Conference on Computer Vision: 
 * Part III (ECCV '08), David Forsyth, Philip Torr, and Andrew Zisserman (Eds.). 
 * Springer-Verlag, Berlin, Heidelberg, 386-399. DOI=10.1007/978-3-540-88690-7_29 
 * http://dx.doi.org/10.1007/978-3-540-88690-7_29
 * 
 * Note that this is not scale invariant - you will get different results with
 * different sized images...
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Reference(
		type = ReferenceType.Inproceedings,
		author = { "Luo, Yiwen", "Tang, Xiaoou" },
		title = "Photo and Video Quality Evaluation: Focusing on the Subject",
		year = "2008",
		booktitle = "Proceedings of the 10th European Conference on Computer Vision: Part III",
		pages = { "386", "", "399" },
		url = "http://dx.doi.org/10.1007/978-3-540-88690-7_29",
		publisher = "Springer-Verlag",
		series = "ECCV '08",
		customData = { 
				"isbn", "978-3-540-88689-1", 
				"location", "Marseille, France", 
				"numpages", "14", 
				"doi", "10.1007/978-3-540-88690-7_29", 
				"acmid", "1478204", 
				"address", "Berlin, Heidelberg" 
		}
	)
public class DepthOfFieldEstimator implements SaliencyMapGenerator<FImage> {
	private static FConvolution DX_FILTER = new FConvolution(new float[][] {{1, -1}});
	private static FConvolution DY_FILTER = new FConvolution(new float[][] {{1}, {-1}});
	
	protected int maxKernelSize = 50;
	protected int kernelSizeStep = 1;
	protected int nbins = 41;
	
	protected int windowSize = 3;
	
	protected float[][] xHistograms;
	protected float[][] yHistograms;
	private FImage map;
	
	/**
	 * Construct with the given parameters.
	 * @param maxKernelSize Maximum kernel size.
	 * @param kernelSizeStep Kernel step size.
	 * @param nbins Number of bins.
	 * @param windowSize window size.
	 */
	public DepthOfFieldEstimator(int maxKernelSize, int kernelSizeStep, int nbins, int windowSize) {
		this.maxKernelSize = maxKernelSize;
		this.kernelSizeStep = kernelSizeStep;
		this.nbins = nbins;
		this.windowSize = windowSize;
		this.xHistograms = new float[maxKernelSize / kernelSizeStep][nbins];
		this.yHistograms = new float[maxKernelSize / kernelSizeStep][nbins];
	}
	
	/**
	 * Construct with the default values (max kernel size = 50, step size = 1, 41 bins, window size of 3).
	 */
	public DepthOfFieldEstimator() {
		this.xHistograms = new float[maxKernelSize / kernelSizeStep][nbins];
		this.yHistograms = new float[maxKernelSize / kernelSizeStep][nbins];
	}
	
	protected void clearHistograms() {
		for (float [] h : xHistograms)
			Arrays.fill(h, 0);
		
		for (float [] h : yHistograms)
			Arrays.fill(h, 0);
	}
	
	/* (non-Javadoc)
	 * @see org.openimaj.image.processor.ImageProcessor#processImage(org.openimaj.image.Image)
	 */
	@Override
	public void analyseImage(FImage image) {
		clearHistograms();
		
		for (int i=0; i<maxKernelSize; i+=kernelSizeStep) {
			FImage blurred = image.process(new AverageBoxFilter(i+1, i+1));
			FImage dx = blurred.process(DX_FILTER);
			FImage dy = blurred.process(DY_FILTER);
			
			makeLogHistogram(xHistograms[i], dx);
			makeLogHistogram(yHistograms[i], dy);
		}
		
		FImage dx = image.process(DX_FILTER);
		FImage dy = image.process(DY_FILTER);
		map = new FImage(image.width, image.height);
		for (int y=0; y<image.height; y++) {
			for (int x=0; x<image.width; x++) {
				if (x == 0 || y == 0 || x == image.width-1 || y == image.height-1) {
					map.pixels[y][x] = maxKernelSize;
				} else {
					int bestModel = 0;
					double bestLL = calculatedLogLikelihood(x, y, dx, dy, 0);
						
					for (int i=1; i<maxKernelSize; i+=kernelSizeStep) {
						double newLL = calculatedLogLikelihood(x, y, dx, dy, i);
						
						if (newLL > bestLL) {
							bestLL = newLL;
							bestModel = i;
						}
					}
					
					map.pixels[y][x] = bestModel;
				}
			}
		}
	}

	private double calculatedLogLikelihood(int x, int y, FImage dx, FImage dy, int level) {
		int border = windowSize / 2;
		
		double LL = 0;
		for (int j=y-border; j<=y+border; j++) {
			for (int i=x-border; i<=x+border; i++) {
				float vx = (dx.pixels[j][i] + 1) / 2;
				int bx = (int) (vx * nbins);
				if (bx >= nbins) bx --;
				
				float vy = (dy.pixels[j][i] + 1) / 2;
				int by = (int) (vy * nbins);
				if (by >= nbins) by --;
				
				LL += xHistograms[level][bx] + yHistograms[level][by];
			}
		}
		return LL;
	}
	
	private void makeLogHistogram(float[] h, FImage im) {
		int sum = 0;
		for (int y=0; y<im.height; y++) {
			for (int x=0; x<im.width; x++) {
				float v = (im.pixels[y][x] + 1) / 2; //norm to 0..1
				
				int bin = (int) (v * nbins);
				if (bin >= nbins) bin --;
				
				h[bin]++;
				sum++;
			}
		}
		
		for (int i=0; i<nbins; i++) {
			if (h[i] == 0) 
				h[i] = 0.00000001f; //a really small value for smoothing
			
			h[i] = (float) Math.log(h[i] / (double)sum);
		}
	}
	
	@Override
	public FImage getSaliencyMap() {
		return map;
	}
}
