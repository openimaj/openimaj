/**
 * 
 */
package org.openimaj.video.timecode;

/**
 * 	A timecode representation that extends the standard frame count representation
 * 	to provide hours, minutes, seconds and frames timecode.
 * 
 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 6 Jun 2011
 */
public class HrsMinSecFrameTimecode extends FrameNumberVideoTimecode
{
	/** The number of frames per second */
	private double framesPerSec = 25;
	
	/**
	 * 	Default constructor that takes the current (start) frame number
	 * 	and the number of frames in one second.
	 * 
	 *  @param number The frame number.
	 *  @param framesPerSec The number of frames per second.
	 */
	public HrsMinSecFrameTimecode( int number, double framesPerSec )
	{
		super( number );		
	}

	/**
	 * 	Get the number of hours.
	 *  @return The number of hours.
	 */
	public int getHours()
	{
		return (int)(getFrameNumber() / framesPerSec / 3600d);		
	}
	
	/**
	 * 	Get the number of minutes within the hour.
	 *  @return The number of minutes within the hour.
	 */
	public int getMinutes()
	{
		return (int)(getFrameNumber() / framesPerSec / 60d) % 60;
	}
	
	/**
	 * 	Get the number of seconds within the minute.
	 *  @return The number of seconds within the minute.
	 */
	public int getSeconds()
	{
		return (int)(getFrameNumber() / framesPerSec) % 60;
	}
	
	/**
	 * 	Get the number of frames within the second.
	 *  @return The number of frames within the second.
	 */
	public int getFrames()
	{
		return (int)(getFrameNumber() % framesPerSec);
	}

	/**
	 *  @inheritDoc
	 *  @see org.openimaj.video.timecode.FrameNumberVideoTimecode#toString()
	 */
	@Override
	public String toString()
	{
	    return getHours()+":"+getMinutes()+":"+getSeconds()+":"+getFrames();
	}
}
