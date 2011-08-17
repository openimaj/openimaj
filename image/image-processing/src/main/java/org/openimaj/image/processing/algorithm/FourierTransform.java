package org.openimaj.image.processing.algorithm;

import org.openimaj.image.FImage;

import edu.emory.mathcs.jtransforms.fft.FloatFFT_2D;

public class FourierTransform {
	FImage phase;
	FImage magnitude;
	boolean centre;

	public FourierTransform(FImage image, boolean centre) {
		this.centre = centre;

		process(image);
	}

	private void process(FImage image) {
		int cs = image.getCols();
		int rs = image.getRows();
		
		phase = new FImage(cs, rs);
		magnitude = new FImage(cs, rs);
		
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
				float re = prepared[y][x*2];
				float im = prepared[y][1 + x*2];
				
				phase.pixels[y][x] = (float) Math.atan2(im, re);
				magnitude.pixels[y][x] = (float) Math.sqrt(re*re + im*im);
			}
		}
	}
	
	public FImage inverse() {
		int cs = magnitude.getCols();
		int rs = magnitude.getRows();
		
		FloatFFT_2D fft = new FloatFFT_2D(rs,cs);
		float[][] prepared = new float[rs][cs*2];
		for(int y = 0; y < rs; y++) {
			for(int x = 0; x < cs; x++) {
				float p = phase.pixels[y][x];
				float m = magnitude.pixels[y][x];

				float re = (float) (m*Math.cos(p));
				float im = (float) (m*Math.sin(p));
				
				prepared[y][x*2] = re;
				prepared[y][1 + x*2] = im;				
			}
		}
		
		fft.complexInverse(prepared, true);
		
		FImage image = new FImage(cs, rs);
		for(int r = 0; r < rs ; r++){
			for(int c = 0; c < cs; c++){
				image.pixels[r][c] = prepared[r][c*2];
			}
		}
		
		return image;
	}
}
