/**
 * 
 */
package org.openimaj.audio;

import java.util.EventListener;

/**
 * 	An interface for objects that wish to be informed that samples are available
 * 	from an {@link AudioGrabber}.	
 * 
 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 28 Oct 2011
 */
public interface AudioGrabberListener extends EventListener
{
	/**
	 * 	Called by an {@link AudioGrabber} when a new sample chunk is available
	 * 	for processing.
	 * 
	 *  @param s The sample chunk
	 */
	public void samplesAvailable( SampleChunk s );
}
