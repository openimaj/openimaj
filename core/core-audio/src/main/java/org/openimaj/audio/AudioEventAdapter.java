/**
 * 
 */
package org.openimaj.audio;

/**
 *	An adapter for audio events that has no implementation.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 28 Jun 2012
 *	@version $Author$, $Revision$, $Date$
 */
public abstract class AudioEventAdapter implements AudioEventListener
{
	/** 
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.AudioEventListener#audioEnded()
	 */
	@Override
	public void audioEnded()
	{
	}
}
