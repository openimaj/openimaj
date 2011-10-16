package org.openimaj.image.processing.convolution.filterbank;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;

/**
 * Implementation of the MR8 filter bank described at:
 * http://www.robots.ox.ac.uk/~vgg/research/texclass/filters.html
 * 
 * This is the naive implementation and as such is quite slow.
 *  
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 */
public class MR8FilterBank extends RootFilterSetFilterBank {
	
	/* (non-Javadoc)
	 * @see org.openimaj.image.processor.ImageProcessor#processImage(org.openimaj.image.Image, org.openimaj.image.Image<?,?>[])
	 */
	@Override
	public void processImage(FImage image, Image<?, ?>... otherimages) {
		FImage[] allresponses = new FImage[filters.length];
		responses = new FImage[8];
		
		for (int i=0; i<filters.length; i++) {
			allresponses[i] = image.process(filters[i]);
		}
				
		int allIndex=0;
		int idx = 0;
		for (int type=0; type<2; type++) {
			for (int scale=0; scale<SCALES.length; scale++) {
				responses[idx] = allresponses[allIndex];
				allIndex++;
				
				for (int orient=1; orient<NUM_ORIENTATIONS; orient++) {
					for (int y=0; y<image.height; y++) {
						for (int x=0; x<image.width; x++) {
							responses[idx].pixels[y][x] = Math.max(responses[idx].pixels[y][x], allresponses[allIndex].pixels[y][x]); 
						}
					}
					
					allIndex++;
				}
				
				idx++;
			}
		}
		
		responses[idx++] = allresponses[allIndex++];
		responses[idx++] = allresponses[allIndex++];
	}
}
