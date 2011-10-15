package org.openimaj.image.processing.convolution.filterbank;

import java.io.File;
import java.io.IOException;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.ImageUtilities;

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
			DisplayUtilities.display(allresponses[i]);
		}
				
		int allIndex=0;
		int idx = 0;
		for (int type=0; type<2; type++) {
			for (int scale=0; scale<SCALEX.length; scale++) {
				responses[idx] = allresponses[allIndex];
				allIndex++;
				
				for (int orient=1; orient<NORIENT; orient++) {
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
	
	public static void main(String[] args) throws IOException {
		FImage test = ImageUtilities.readF(new File("/Users/jon/Downloads/codetsu/TextureToolBox/5_0001.jpg"));
		
		MR8FilterBank fb = new MR8FilterBank();
		test.processInline(fb);
		
		
	}
}
