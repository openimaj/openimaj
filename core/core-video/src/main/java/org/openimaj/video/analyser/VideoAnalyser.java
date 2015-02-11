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
package org.openimaj.video.analyser;

import org.openimaj.image.Image;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.video.Video;
import org.openimaj.video.processor.VideoProcessor;

/**
 * This class is analagous to the {@link ImageAnalyser} class for analysing
 * image. It should be used to make analysis and measurements on video frames
 * without altering the frames themselves.
 * <p>
 * This class overrides {@link VideoProcessor} to inherit many of its abilities
 * (chainability etc.) but the processFrame method will always return the input
 * frame having called analyseFrame. The analyseFrame method should not alter
 * the input frame.
 *
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 1 Mar 2012
 *
 * @param <T>
 *            The type of the video frame to be analysed
 */
public abstract class VideoAnalyser<T extends Image<?, T>>
		extends VideoProcessor<T>
{
	/**
	 * Construct a stand-alone video analyser.
	 */
	public VideoAnalyser()
	{
	}

	/**
	 * Construct a chainable video analyser.
	 *
	 * @param v
	 *            The video to chain to
	 */
	public VideoAnalyser(Video<T> v)
	{
		super(v);
	}

	/**
	 * Analyse the given frame and make no changes to the frame.
	 *
	 * @param frame
	 *            The video frame to analyse
	 */
	public abstract void analyseFrame(T frame);

	/**
	 * This method will return the input frame after calling
	 * {@link #analyseFrame(Image)} on the input image.
	 *
	 * @see org.openimaj.video.processor.VideoProcessor#processFrame(org.openimaj.image.Image)
	 */
	@Override
	final public T processFrame(T frame)
	{
		analyseFrame(frame);
		return frame;
	}
}
