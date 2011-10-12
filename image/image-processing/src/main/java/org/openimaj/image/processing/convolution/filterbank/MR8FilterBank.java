package org.openimaj.image.processing.convolution.filterbank;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;

public class MR8FilterBank extends RootFilterSetFilterBank {
	
	/* (non-Javadoc)
	 * @see org.openimaj.image.processor.ImageProcessor#processImage(org.openimaj.image.Image, org.openimaj.image.Image<?,?>[])
	 */
	@Override
	public void processImage(FImage image, Image<?, ?>... otherimages) {
		FImage[] allresponses = new FImage[filters.length];
		
		for (int i=0; i<filters.length; i++) {
			allresponses[i] = image.process(filters[i]);
		}
		
		//now find the max response/orientation
		
		for (int scale=0; scale<SCALEX.length; scale++) {
			for (int orient=0; orient<NORIENT; orient++) {
				
			}
		}
	}
}
