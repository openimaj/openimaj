/**
 *
 */
package org.openimaj.vis.general;

import org.openimaj.image.Image;
import org.openimaj.vis.general.XYPlotVisualisation.LocatedObject;

/**
 *	An interface for classes that are able to plot items into a visualisation
 *	using an {@link AxesRenderer} to determine the position.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 * 	@param <O> The type of object to be plotted
 * 	@param <Q> The type of pixel that the image needs to be
 * 	@param <I> The type of image being plotted to
 *  @created 3 Jun 2013
 */
public interface ItemPlotter<O,Q,I extends Image<Q,I>>
{
	/**
	 * 	Called just before a render of the visualisation is about to start.
	 * 	This can be used to prepare context objects for the plotObject method.
	 */
	public abstract void renderRestarting();

	/**
	 * 	Plots a specific object to the visualisation using the {@link AxesRenderer} to
	 * 	provide the position of the object. Should side affect the given image.
	 *
	 *	@param visImage The image to draw to
	 *	@param object The object
	 *	@param renderer The axes renderer
	 */
	public abstract void plotObject( I visImage,
			LocatedObject<O> object, AxesRenderer<Q,I> renderer );
}
