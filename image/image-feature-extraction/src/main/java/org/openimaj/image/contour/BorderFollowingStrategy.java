package org.openimaj.image.contour;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.util.function.Operation;
import org.openimaj.util.pair.IndependentPair;

/**
 * A Border Following strategy implements a Contour Tracing algorithm that
 * extracts a boundary from an image
 * 
 * Many examples can be found here:
 * http://www.imageprocessingplace.com/downloads_V3
 * /root_downloads/tutorials/contour_tracing_Abeer_George_Ghuneim/index.html
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public abstract class BorderFollowingStrategy {

	/**
	 * Follow the border, adding each pixel to a list. The first pixel in the
	 * list is guaranteed to the be equal to start
	 * 
	 * @param image
	 * @param start
	 * @param from
	 * @return a list of border pixels in the image starting from the start
	 *         pixel
	 */
	public List<Pixel> border(FImage image, Pixel start, Pixel from) {
		final List<Pixel> ret = new ArrayList<Pixel>();
		border(image, start, from, new Operation<Pixel>() {

			@Override
			public void perform(Pixel object) {
				ret.add(object);
			}

		});
		return ret;
	}

	/**
	 * 
	 * Given some starting pixel in an image on a border and the direction of a
	 * non starting image, return each pixel on a border from the start pixel in
	 * the image. The first pixel returned must be the start pixel
	 * 
	 * @param image
	 *            the image
	 * @param start
	 *            the first point on the border
	 * @param from
	 *            the pixel that was not a border
	 * @param operation
	 *            the thing to do for each border pixel found
	 */
	public void border(FImage image, Pixel start, Pixel from, final Operation<Pixel> operation) {
		directedBorder(image, start, from, new Operation<IndependentPair<Pixel, DIRECTION>>() {

			@Override
			public void perform(IndependentPair<Pixel, DIRECTION> object) {
				operation.perform(object.firstObject());
			}
		});
	}

	/**
	 * Given some starting pixel in an image on a border and the direction of a
	 * non starting image, return each pixel on a border from the start pixel in
	 * the image. The first pixel returned must be the start pixel. This
	 * function must be functionally the same as
	 * {@link #border(FImage, Pixel, Pixel, Operation)} but should return a
	 * {@link Pixel} {@link DIRECTION} pair such that the {@link DIRECTION}
	 * defines the location of the background which this pixel is a border
	 * against.
	 * 
	 * @param image
	 *            the image
	 * @param start
	 *            the first point on the border
	 * @param from
	 *            the pixel that was not a border
	 * @param operation
	 *            the thing to do for each border pixel found
	 */
	public abstract void directedBorder(FImage image, Pixel start, Pixel from,
			Operation<IndependentPair<Pixel, DIRECTION>> operation);

}
