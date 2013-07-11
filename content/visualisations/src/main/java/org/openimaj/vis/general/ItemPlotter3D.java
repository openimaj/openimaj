/**
 *
 */
package org.openimaj.vis.general;

import javax.media.opengl.GLAutoDrawable;

import org.openimaj.vis.general.XYZVisualisation3D.LocatedObject3D;

/**
 *	An interface for classes that are able to plot items into a 3D visualisation
 *	using an {@link AxesRenderer3D} to determine the position.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 11 Jul 2013
 *	@version $Author$, $Revision$, $Date$
 * 	@param <O>
 */
public interface ItemPlotter3D<O>
{
	/**
	 * 	Called just before a render of the visualisation is about to start.
	 * 	This can be used to prepare context objects for the plotObject method.
	 */
	public abstract void renderRestarting();

	/**
	 * 	Plots a specific object to the 3D visualisation using the {@link AxesRenderer3D} to
	 * 	provide the position of the object. Should side affect the given image.
	 *
	 * 	@param drawable The 3D world
	 *	@param object The object
	 *	@param renderer The axes renderer
	 */
	public abstract void plotObject( GLAutoDrawable drawable,
			LocatedObject3D<O> object, AxesRenderer3D renderer );
}
