/**
 * 
 */
package org.openimaj.audio.generation;

import java.util.EventListener;

/**
 *	An interface for objects that want to listen for synthesizer events.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 13 Feb 2013
 *	@version $Author$, $Revision$, $Date$
 */
public interface SynthesizerListener extends EventListener
{
	/**
	 * 	Called when the synthesizer goes quiet (gets to the end of a release)
	 */
	public void synthQuiet();
}
