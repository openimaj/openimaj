package org.openimaj.image.processing.convolution;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.processor.SinglebandImageProcessor;

import cern.colt.Arrays;

import Jama.Matrix;

import edu.emory.mathcs.jtransforms.fft.FloatFFT_2D;

/**
 * From the matlab implementation of DISCGAUSSFFT which uses an FFT to apply a gaussin kernel.
 * The matlab docs:
 * 
% DISCGAUSSFFT(pic, sigma2) -- Convolves an image by the
% (separable) discrete analogue of the Gaussian kernel by
% performing the convolution in the Fourier domain.
% The parameter SIGMA2 is the variance of the kernel.

% Reference: Lindeberg "Scale-space theory in computer vision", Kluwer, 1994.
 * 
 * @author Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class FDiscGausConvolve implements SinglebandImageProcessor<Float, FImage> {
	private float sigma2;

	public FDiscGausConvolve(float sigma2){
		this.sigma2 = sigma2;
//		this.fft = new FastFourierTransformer();
	}

	@Override
	public void processImage(FImage image, Image<?, ?>... otherimages) {
		int cs = image.getCols();
		int rs = image.getRows();
		FloatFFT_2D fft = new FloatFFT_2D(rs,cs);
		float[][] prepared = new float[rs][cs*2];
		for(int r = 0; r < rs ; r++){
			for(int c = 0; c < cs; c++){
				prepared[r][c*2] = image.pixels[r][c];
				prepared[r][1 + c*2] = 0;
			}
		}
		fft.complexForward(prepared);
		for(int y = 0; y < rs; y++){
			for(int x = 0; x < cs; x++){
				double xcos = Math.cos(2 * Math.PI * ((float)x/cs));
				double ycos = Math.cos(2 * Math.PI * ((float)y/rs));
				float multiply = (float) Math.exp(sigma2 * (xcos + ycos - 2));
				prepared[y][x*2] = prepared[y][x*2] * multiply;
				prepared[y][1 + x*2] = prepared[y][1 + x*2] * multiply;
			}
		}
		fft.complexInverse(prepared, true);
		for(int r = 0; r < rs ; r++){
			for(int c = 0; c < cs; c++){
				image.pixels[r][c] = prepared[r][c*2];
			}
		}
	}
	
	public static void main(String[] args){
		FImage f = new FImage(new float[][]{
				{0.9340f,0.3371f,0.1656f,0.7482f,0.1524f,0.4427f,0.8173f},
				{0.1299f,0.1622f,0.6020f,0.4505f,0.8258f,0.1067f,0.8687f},
				{0.5688f,0.7943f,0.2630f,0.0838f,0.5383f,0.9619f,0.0844f},
				{0.4694f,0.3112f,0.6541f,0.2290f,0.9961f,0.0046f,0.3998f},
				{0.0119f,0.5285f,0.6892f,0.9133f,0.0782f,0.7749f,0.2599f},
		});
		f.processInline(new FDiscGausConvolve(4));
		/**
		 * According to matlab this should be something like:
		 * ans =

    0.4620    0.4619    0.4674    0.4740    0.4765    0.4735    0.4673
    0.4614    0.4602    0.4651    0.4720    0.4758    0.4737    0.4674
    0.4587    0.4588    0.4645    0.4714    0.4745    0.4713    0.4642
    0.4575    0.4595    0.4664    0.4730    0.4744    0.4695    0.4619
    0.4595    0.4614    0.4683    0.4747    0.4756    0.4709    0.4638
		 */
		System.out.println(f);
	}
}
