package org.openimaj.image.contour;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.util.function.Operation;

/**
 * A Border Following strategy implements a Contour Tracing algorithm that
 * extracts a boundary from an image
 * 
 * Many examples can be found here:
 * http://www.imageprocessingplace.com/downloads_V3/root_downloads/tutorials/contour_tracing_Abeer_George_Ghuneim/index.html
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public abstract class BorderFollowingStrategy {
	
	/**
	 * Follow the border, adding each pixel to a list. The first pixel
	 * in the list is guaranteed to the be equal to start
	 * @param image
	 * @param start
	 * @return a list of border pixels in the image starting from the start pixel
	 */
	public List<Pixel> border(FImage image, Pixel start){
		final List<Pixel> ret = new ArrayList<Pixel>();
		border(image, start, new Operation<Pixel>(){

			@Override
			public void perform(Pixel object) {
				ret.add(object);
			}
			
		});
		return ret;
	}

	/**
	 * Given some starting pixel on an image, return each pixel on
	 * a border from the start pixel in the image. The first pixel 
	 * returned must be the start pixel
	 * @param image
	 * @param start
	 * @param operation
	 */
	public abstract void border(FImage image, Pixel start, Operation<Pixel> operation) ;
	

}
