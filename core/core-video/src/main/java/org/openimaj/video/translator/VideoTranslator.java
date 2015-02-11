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
package org.openimaj.video.translator;

import org.openimaj.image.Image;
import org.openimaj.video.Video;

/**
 * A video translator is a video processor where the input and output frame
 * types may be different. This means that no processing can take place in place
 * but new frames must be returned.
 *
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 1 Mar 2012
 *
 *
 * @param <INPUT>
 * @param <OUTPUT>
 */
public abstract class VideoTranslator<INPUT extends Image<?, INPUT>, OUTPUT extends Image<?, OUTPUT>>
		extends Video<OUTPUT>
{
	/** The input video */
	private Video<INPUT> video = null;

	/** The last processed frame */
	private OUTPUT currentFrame = null;

	/**
	 * Construct a new VideoTranslator that will translate the given input
	 * video.
	 *
	 * @param in
	 *            The input video.
	 */
	public VideoTranslator(Video<INPUT> in)
	{
		this.video = in;
	}

	@Override
	public double getFPS()
	{
		return video.getFPS();
	}

	@Override
	public OUTPUT getCurrentFrame()
	{
		return currentFrame;
	}

	@Override
	public int getWidth()
	{
		return video.getWidth();
	}

	@Override
	public int getHeight()
	{
		return video.getHeight();
	}

	@Override
	public long getTimeStamp()
	{
		return video.getTimeStamp();
	}

	@Override
	public boolean hasNextFrame()
	{
		return video.hasNextFrame();
	}

	@Override
	public long countFrames()
	{
		return video.countFrames();
	}

	@Override
	public void reset()
	{
		video.reset();
	}

	@Override
	public OUTPUT getNextFrame()
	{
		return currentFrame = translateFrame(video.getNextFrame());
	}

	/**
	 * Translate the given input frame to the appropriate output frame.
	 *
	 * @param nextFrame
	 *            The input frame.
	 * @return The output frame
	 */
	public abstract OUTPUT translateFrame(INPUT nextFrame);
}
