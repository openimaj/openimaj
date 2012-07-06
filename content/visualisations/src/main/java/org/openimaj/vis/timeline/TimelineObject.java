/**
 * 
 */
package org.openimaj.vis.timeline;

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
}
