/**
 * 
 */
package org.openimaj.audio.timecode;

import org.openimaj.time.Timecode;

/**
 * 	A basic audio timecode that represents the number of milliseconds from
 * 	the start of the audio file.
 * 
 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 21 Nov 2011
 */
public class AudioTimecode implements Timecode
{
	/** The number of milliseconds from the start of the audio file */
	protected long milliseconds = 0;
	
	/**
	 * 	Default constructor that takes the number of milliseconds
	 * 	into the audio file.
	 * 
	 *  @param milliseconds The number of milliseconds from the start of the
	 *  	audio file.
	 */
	public AudioTimecode( long milliseconds )
    {
		this.milliseconds = milliseconds;
    }
	
	/**
	 *  @inheritDoc
	 *  @see org.openimaj.time.Timecode#getTimecodeInMilliseconds()
	 */
	@Override
	public long getTimecodeInMilliseconds()
	{
		return milliseconds;	
	}

	/**
	 *  @inheritDoc
	 *  @see org.openimaj.time.Timecode#setTimecodeInMilliseconds(long)
	 */
	@Override
	public void setTimecodeInMilliseconds( long timeInMilliseconds )
	{
		this.milliseconds = timeInMilliseconds;
	}
	
	/**
	 *  @inheritDoc
	 *  @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
	    return "AudioTimecode: "+this.milliseconds;
	}
	
	/**
	 *  @inheritDoc
	 *  @see java.lang.Object#clone()
	 */
	@Override
	public AudioTimecode clone()
	{
		return new AudioTimecode( milliseconds );
	}
}
