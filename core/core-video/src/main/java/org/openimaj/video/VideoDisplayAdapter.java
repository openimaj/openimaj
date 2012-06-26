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
package org.openimaj.video;

import org.openimaj.image.Image;
import org.openimaj.video.VideoDisplay.Mode;

/**
 *	An adapter for classes that want to listen to certain events that are
 *	generated from a video display. This adapter implements both the
 *	{@link VideoDisplayListener} and the {@link VideoDisplayStateListener}.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 27 Jul 2011
 *	
 *  @param <T> the {@link Image} type 
 */
public class VideoDisplayAdapter<T extends Image<?,T>> 
	implements VideoDisplayStateListener, VideoDisplayListener<T>
{

	/** 
	 *	{@inheritDoc}
	 * 	@see org.openimaj.video.VideoDisplayStateListener#videoStopped(org.openimaj.video.VideoDisplay)
	 */
	@Override
	public void videoStopped( VideoDisplay<?> v )
	{
	}

	/** 
	 *	{@inheritDoc}
	 * 	@see org.openimaj.video.VideoDisplayStateListener#videoPlaying(org.openimaj.video.VideoDisplay)
	 */
	@Override
	public void videoPlaying( VideoDisplay<?> v )
	{
	}

	/** 
	 *	{@inheritDoc}
	 * 	@see org.openimaj.video.VideoDisplayStateListener#videoPaused(org.openimaj.video.VideoDisplay)
	 */
	@Override
	public void videoPaused( VideoDisplay<?> v )
	{
	}

	/** 
	 *	{@inheritDoc}
	 * 	@see org.openimaj.video.VideoDisplayStateListener#videoStateChanged(org.openimaj.video.VideoDisplay.Mode, org.openimaj.video.VideoDisplay)
	 */
	@Override
	public void videoStateChanged( Mode mode, VideoDisplay<?> v )
	{
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.video.VideoDisplayListener#afterUpdate(org.openimaj.video.VideoDisplay)
	 */
	@Override
	public void afterUpdate( VideoDisplay<T> display )
	{
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.video.VideoDisplayListener#beforeUpdate(org.openimaj.image.Image)
	 */
	@Override
	public void beforeUpdate( T frame )
	{
	}
}
