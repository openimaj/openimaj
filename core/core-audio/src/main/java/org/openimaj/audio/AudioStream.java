/**
 * 
 */
package org.openimaj.audio;

/**
 *	Represents an audio stream that can be read chunk-by-chunk.
 *
 *	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *  @created 8 Jun 2011
 *	@version $Author$, $Revision$, $Date$
 */
public abstract class AudioStream extends Audio
{
	/**
	 * 	Retrieve the next SampleChunk from the audio stream.
	 *	@return The next sample chunk in the audio stream.
	 */
	public abstract SampleChunk nextSampleChunk();
}
