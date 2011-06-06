/**
 * 
 */
package org.openimaj.video.timecode;

/**
 * 	This class allows video timecodes to be stored as frame numbers.
 * 
 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 1 Jun 2011
 */
public class FrameNumberVideoTimecode extends VideoTimecode
{
	/** The frame number */
	private int frameNumber = -1;
	
	/**
	 * 	Default constructor that takes the frame number.
	 *  @param number The frame number
	 */
	public FrameNumberVideoTimecode( int number )
	{
		this.frameNumber = number;
	}
	
	/**
	 * 	Get the frame number in this timecode.
	 *  @return The frame number in this timecode.
	 */
	public int getFrameNumber()
	{
		return this.frameNumber;
	}
	
	/**
	 * 	Set the frame number.
	 *  @param frame The frame number
	 */
	public void setFrameNumber( int frame )
	{
		this.frameNumber = frame;
	}

	/**
	 *  @inheritDoc
	 *  @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
    public int compareTo( VideoTimecode o )
    {
		if( o instanceof FrameNumberVideoTimecode )
			return ((FrameNumberVideoTimecode)o).getFrameNumber() - this.frameNumber;
		
	    return 0;
    }

	/**
	 *  @inheritDoc
	 *  @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "Frame "+this.frameNumber;
	}
}
