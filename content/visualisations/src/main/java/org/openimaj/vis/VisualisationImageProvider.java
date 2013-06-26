/**
 *
 */
package org.openimaj.vis;

import java.awt.Dimension;

import org.openimaj.image.MBFImage;

/**
 *	An interface for objects that can provide a static image of a visualisation
 *	through the {@link #getVisualisationImage()}. If you need to force a redraw
 *	(or an initial draw) of the visualisation, you may call {@link #updateVis()}.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 25 Jun 2013
 *	@version $Author$, $Revision$, $Date$
 */
public interface VisualisationImageProvider
{
	/**
	 * 	Force a redraw of the visualisation image.
	 */
	public void updateVis();

	/**
	 * 	Return the static visualisation image.
	 *	@return The visualisation image.
	 */
	public MBFImage getVisualisationImage();

	/**
	 * 	Set the required size of the visualisation image.
	 *	@param d The dimensions
	 */
	public void setRequiredSize( Dimension d );
}
