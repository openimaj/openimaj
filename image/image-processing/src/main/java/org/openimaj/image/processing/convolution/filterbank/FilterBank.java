package org.openimaj.image.processing.convolution.filterbank;

import org.openimaj.feature.FloatFV;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.processing.convolution.FConvolution;
import org.openimaj.image.processor.ImageProcessor;

public abstract class FilterBank implements ImageProcessor<FImage> {
	protected FConvolution [] filters;
	protected FImage[] responses;
	
	/* (non-Javadoc)
	 * @see org.openimaj.image.processor.ImageProcessor#processImage(org.openimaj.image.Image, org.openimaj.image.Image<?,?>[])
	 */
	@Override
	public void processImage(FImage image, Image<?, ?>... otherimages) {
		responses = new FImage[filters.length];
		
		for (int i=0; i<filters.length; i++) {
			responses[i] = image.process(filters[i]);
		}
	}

	public FImage[] getResponseImages() {
		return responses;
	}
	
	public float[] getResponse(int x, int y) {
		float[] response = new float[responses.length];
		
		for (int i=0; i<response.length; i++)
			response[i] = responses[i].getPixelNative(x, y);
		
		return response;
	}
	
	public FloatFV getResponseFV(int x, int y) {
		return new FloatFV(getResponse(x, y));
	}
}
