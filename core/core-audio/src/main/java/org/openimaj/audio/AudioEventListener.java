/**
 * 
 */
package org.openimaj.audio;

/**
 *	An interface for events that may occur during audio streaming.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 28 Jun 2012
 *	@version $Author$, $Revision$, $Date$
 */
public interface AudioEventListener
{
	/**
	 * 	Called when the audio stream reaches the end.
	 */
	public void audioEnded();
}
