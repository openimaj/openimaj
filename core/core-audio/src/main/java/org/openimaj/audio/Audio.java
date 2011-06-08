/**
 * 
 */
package org.openimaj.audio;

/**
 *	An abstract class representing some form of audio data. This class
 *	encapsulates the audio description information.
 *
 *	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *  @created 8 Jun 2011
 *	@version $Author$, $Revision$, $Date$
 */
public abstract class Audio
{
	/** The format of the audio in this audio object */
	protected AudioFormat format = null;

	/**
	 * 	Set the audio format of this audio object.
	 *	@param format The format of the audio data
	 */
	public void setFormat( AudioFormat format )
	{
		this.format = format;
	}

	/**
	 * 	Get the audio format of this audio object.
	 *	@return The format of this audio data
	 */
	public AudioFormat getFormat()
	{
		return format;
	}
	
	/**
	 * 	Returns the samples from the audio
	 *	@return the samples from the audio
	 */
	public abstract SampleChunk getSampleChunk();
}
