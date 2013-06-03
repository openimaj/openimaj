/**
 *
 */
package org.openimaj.vis.general;

import org.openimaj.image.MBFImage;
import org.openimaj.vis.general.XYPlotVisualisation.LocatedObject;

/**
 *
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 * 	@param <O> The type of object to be plotted
 * 	@param <Q> The type of pixel that the image needs to be
 *  @created 3 Jun 2013
 */
public interface ItemPlotter<O,Q>
{
	/**
	 * 	Plots a specific object to the visualisation using the {@link AxesRenderer} to
	 * 	provide the position of the object. Should side affect the given image.
	 *
	 *	@param visImage The image to draw to
	 *	@param object The object
	 *	@param renderer The axes renderer
	 */
	public abstract void plotObject( MBFImage visImage,
			LocatedObject<O> object, AxesRenderer<Q> renderer );

}
