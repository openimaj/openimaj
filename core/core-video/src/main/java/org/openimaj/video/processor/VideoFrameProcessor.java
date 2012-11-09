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
package org.openimaj.video.processor;

import org.openimaj.image.Image;
import org.openimaj.image.processor.ImageProcessor;
import org.openimaj.video.Video;

/**
 * This class is a {@link VideoProcessor} that uses an {@link ImageProcessor}
 * for processing frames of a video.
 * 
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 27 Jul 2011
 * 
 * 
 * @param <I>
 *            The image type that this processor will process
 */
public class VideoFrameProcessor<I extends Image<?, I>>
		extends VideoProcessor<I>
{
	/** The processor that will be used to process frames */
	private ImageProcessor<I> processor = null;

	/**
	 * Non-chainable constructor
	 * 
	 * @param processor
	 *            the processor to use
	 */
	public VideoFrameProcessor(ImageProcessor<I> processor)
	{
		this.processor = processor;
	}

	/**
	 * Chainable constructor.
	 * 
	 * @param video
	 *            The video to process
	 * @param processor
	 *            the next processor in line
	 */
	public VideoFrameProcessor(Video<I> video, ImageProcessor<I> processor)
	{
		super(video);
		this.processor = processor;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.video.processor.VideoProcessor#processFrame(org.openimaj.image.Image)
	 */
	@Override
	public I processFrame(I frame)
	{
		return frame.process(this.processor);
	}

}
