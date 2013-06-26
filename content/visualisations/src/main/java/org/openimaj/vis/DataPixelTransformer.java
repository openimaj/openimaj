/**
 *
 */
package org.openimaj.vis;

import org.openimaj.image.Image;
import org.openimaj.math.geometry.point.Point2d;

/**
 *	An interface for classes that can transform data units into pixel
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 26 Jun 2013
 *	@version $Author$, $Revision$, $Date$
 * 	@param <I> The image type of the image in which the pixel positions will be valid
 */
public interface DataPixelTransformer<I extends Image<?,I>>
{
	/**
	 * 	Force a precalculation of the variables required for
	 * 	the pixel transformation.
	 * 	@param image The image which will be converted to/from
	 */
	public void precalc( I image );

	/**
	 * 	For a given coordinate in the units of the data, will calculate
	 * 	the pixel position.
	 *
	 * 	@param image The image in which the returned coordinates will be valid
	 *	@param x The x position
	 *	@param y The y position
	 *	@return The pixel position
	 */
	public Point2d calculatePosition(
			final I image, final double x, final double y );

	/**
	 *	Calculates the data unit coordinate at the given pixel position.
	 *
	 *	@param image The image in which the positions were taken
	 *	@param x The x pixel position
	 *	@param y The y pixel position
	 *	@return the x and y unit coordinates in a double
	 */
	public double[] calculateUnitsAt(
			final I image, final int x, final int y );
}
