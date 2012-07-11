/**
 * 
 */
package org.openimaj.vis.timeline;

import java.awt.Dimension;

import javax.swing.JPanel;

/**
 *	A timeline object has a start and end time and is able to be drawn into
 *	a timeline as it extends a JPanel.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 3 Jul 2012
 *	@version $Author$, $Revision$, $Date$
 */
public abstract class TimelineObject extends JPanel
{
	/** */
	private static final long serialVersionUID = 1L;
	
	/** The viewport dimensions */
	private Dimension viewport = null;
	
	/** The start time */
	private long startTime = 0;

	/**
	 * 	Returns the start time of the timeline object in the timeline.
	 *	@return The start time
	 */
	public abstract long getStartTimeMilliseconds();
	
	/**
	 * 	Returns the end time of the timeline object in the timeline.
	 *	@return The end time.
	 */
	public abstract long getEndTimeMilliseconds();
	
	/**
	 * 	Set the part of this timeline that is visible, allowing this timeline
	 * 	object to draw only the part that is visible. The dimension should be
	 * 	in pixels and give the viewport area. The start time gives the time
	 * 	in milliseconds at which 0 coordinate is positioned in time.
	 * 
	 *	@param d The dimension of the viewport.
	 * 	@param startTimeMilliseconds Start time
	 */
	public void setViewSize( Dimension d, long startTimeMilliseconds )
	{
		this.viewport = d;
		this.startTime = startTimeMilliseconds;
	}

	/**
	 * 	Returns the viewport dimensions.
	 *	@return the viewport The viewport dimensions
	 */
	public Dimension getViewSize()
	{
		return viewport;
	}
	
	/**
	 * 	Returns the start time position in milliseconds
	 *	@return the start time position.
	 */
	public long getStartTime()
	{
		return this.startTime;
	}
}
