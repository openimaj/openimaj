/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
	 *	@inheritDoc
	 * 	@see org.openimaj.video.timecode.VideoTimecode#getFrameNumber()
	 */
	@Override
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
