package org.openimaj.image.processing.algorithm;

import java.io.File;
import java.io.IOException;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processor.ImageProcessor;

public class MaskedRobustContrastEqualisation implements ImageProcessor<FImage> {
	double alpha = 0.1;
	double tau = 10;
	
	@Override
	public void processImage(FImage image, Image<?, ?>... otherimages) {
		FImage mask = null;
		if (otherimages.length > 1) {
			mask = (FImage) otherimages[0];
		} else {
			mask = new FImage(image.width, image.height).fill(1f);
		}
		
		//1st pass
		image.divideInline(firstPassDivisor(image, mask));
		
		//2nd pass
		image.divideInline(secondPassDivisor(image, mask));
		
		//3rd pass
		for (int y=0; y<image.height; y++) {
			for (int x=0; x<image.width; x++) {
				if (mask.pixels[y][x] == 1) {
					image.pixels[y][x] = (float) (tau * Math.tanh(image.pixels[y][x] / tau));
				} else {
					image.pixels[y][x] = 0;
				}
			}
		}
	}
	
	float firstPassDivisor(FImage image, FImage mask) {
		double accum = 0;
		int count = 0;
		
		for (int y=0; y<image.height; y++) {
			for (int x=0; x<image.width; x++) {
				if (mask.pixels[y][x] == 1) {
					double ixy = image.pixels[y][x];
					
					accum += Math.pow(Math.abs(ixy), alpha);
					count++;
				}
			}
		}
		
		return (float) Math.pow(accum / count, 1.0 / alpha);
	}
	
	float secondPassDivisor(FImage image, FImage mask) {
		double accum = 0;
		int count = 0;
		
		for (int y=0; y<image.height; y++) {
			for (int x=0; x<image.width; x++) {
				if (mask.pixels[y][x] == 1) {
					double ixy = image.pixels[y][x];
					
					accum += Math.pow(Math.min(tau, Math.abs(ixy)), alpha);
					count++;
				}
			}
		}
		
		return (float) Math.pow(accum / count, 1.0 / alpha);
	}
	
	public static void main(String [] args) throws IOException {
		FImage image = ImageUtilities.readF(new File("/Users/jsh2/Downloads/amfg07-demo-v1/02463d254.pgm"));
		DisplayUtilities.display(image);
		
		image.processInline(new GammaCorrection()).processInline(new DifferenceOfGaussian()).processInline(new MaskedRobustContrastEqualisation());
		DisplayUtilities.display(image.normalise());
		
		FImage image2 = ImageUtilities.readF(new File("/Users/jsh2/Downloads/amfg07-demo-v1/02463d282.pgm"));
		DisplayUtilities.display(image2);
		
		image2.processInline(new GammaCorrection()).processInline(new DifferenceOfGaussian()).processInline(new MaskedRobustContrastEqualisation());
		DisplayUtilities.display(image2.normalise());
	}
}
