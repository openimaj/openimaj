/**
 *
 */
package org.openimaj.vis.timeline;

import java.awt.Dimension;

/**
 *	An object that can be drawn into a timeline needs to be able to provide
 *	the start and end time of its data, and also be able to accept a size
 *	at which it should draw itself.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 19 Jun 2013
 *	@version $Author$, $Revision$, $Date$
 */
public interface TimelineObject
{
	/**
	 * 	Returns the start time of the timeline object in the timeline.
	 *	@return The start time
	 */
	public long getStartTimeMilliseconds();

	/**
	 * 	Returns the end time of the timeline object in the timeline.
	 *	@return The end time.
	 */
	public long getEndTimeMilliseconds();

	/**
	 * 	Set the part of this timeline that is visible, allowing this timeline
	 * 	object to draw only the part that is visible. The dimension should be
	 * 	in pixels and give the viewport area. The start time gives the time
	 * 	in milliseconds at which 0 coordinate is positioned in time.
	 *
	 *	@param d The dimension of the viewport.
	 * 	@param startTimeMilliseconds Start time
	 * 	@param endTimeMilliseconds End Time
	 */
	public void setViewSize( final Dimension d, final long startTimeMilliseconds, long endTimeMilliseconds );

	/**
	 * 	The timeline object needs to be able to suggest a size, if it
	 * 	needs a particular amount. It's not guaranteed it will get this size,
	 * 	but it may ask for it by implementing this method.
	 *	@return A {@link Dimension}
	 */
	public Dimension getPreferredSize();
}
