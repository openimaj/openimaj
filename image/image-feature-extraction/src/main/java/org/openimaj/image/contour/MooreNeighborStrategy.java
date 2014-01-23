package org.openimaj.image.contour;

import org.openimaj.image.FImage;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.util.function.Operation;
import org.openimaj.util.pair.IndependentPair;

/**
 * The Moore Neighborhood border tracing strategy as described by
 * http://www.imageprocessingplace.com/downloads_V3/root_downloads/tutorials/
 * contour_tracing_Abeer_George_Ghuneim/moore.html
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class MooreNeighborStrategy extends BorderFollowingStrategy {

	@Override
	public void directedBorder(FImage image, Pixel start, Pixel from,
			Operation<IndependentPair<Pixel, DIRECTION>> operation)
	{
		Pixel p = start;
		if (image.pixels[start.y][start.x] == 0)
			return;
		DIRECTION cdirStart = DIRECTION.fromTo(p, from);
		operation.perform(IndependentPair.pair(start, cdirStart));
		final DIRECTION firstCdir = cdirStart;
		DIRECTION cdir = cdirStart.clockwise();
		int startCount = 0;
		while (cdir != cdirStart) {
			if (cdir.active(image, p)) {
				final Pixel c = cdir.pixel(p);
				cdirStart = cdir.clockwiseEntryDirection();
				if (c.equals(start)) {
					startCount++;
					if (startCount >= 2 || firstCdir == cdirStart) {
						return;
					}

				}
				operation.perform(IndependentPair.pair(c, cdirStart));
				p = c;
				cdir = cdirStart.clockwise();
			}
			else {
				cdir = cdir.clockwise();
			}
		}
	}

}
