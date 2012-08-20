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
	 * 	Called before a sample chunk has been played.
	 *	@param sc The sample chunk that will be played.
	 */
	public void beforePlay( SampleChunk sc );
	
	/**
	 * 	Called after the audio player given has played a sample chunk
	 *	@param ap The audio player that played a sample chunk
	 *	@param sc The sample chunk last played
	 */
	public void afterPlay( AudioPlayer ap, SampleChunk sc );
	
	/**
	 * 	Called when the audio stream reaches the end.
	 */
	public void audioEnded();
}
