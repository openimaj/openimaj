package org.openimaj.image.processing.algorithm;

import org.openimaj.image.processor.PixelProcessor;

/**
 * Class to perform Gamma correction on a grey-level image.
 * Grey levels are transformed such that I_new = I^Gamma for
 * Gamma > 0 and all pixels I. If Gamma == 0 then the transform
 * is I_new = log(I). 
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class GammaCorrection implements PixelProcessor<Float> {
	protected double gamma;
	
	/**
	 * Construct a GammaCorrection with the default gamma of 0.2
	 */
	public GammaCorrection() {
		this.gamma = 0.2; 
	}
	
	public GammaCorrection(double gamma) {
		this.gamma = gamma; 
	}
	
	@Override
	public Float processPixel(Float pixel, Number[]... otherpixels) {
		if (gamma == 0) {
			return (float) Math.log(pixel);
		}
		return (float) Math.pow(pixel, gamma);
	}
}

