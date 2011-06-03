package org.openimaj.image.processing.algorithm;

import org.openimaj.image.processor.PixelProcessor;

/**
 * Process pixels by raising there value to a value
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class Pow implements PixelProcessor<Float> {
	double power;
	
	/**
	 * Construct with the default power
	 * @param power
	 */
	public Pow(double power) {
		this.power = power;
	}
	
	@Override
	public Float processPixel(Float pixel, Number[]... otherpixels) {
		return (float) Math.pow(pixel, power);
	}
	
}
