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
package org.openimaj.video.processing.shotdetector;

import org.openimaj.image.Image;
import org.openimaj.video.timecode.VideoTimecode;

/**
 * 	Interface for objects that wish to listen to shot detections from
 * 	the shot detector. 
 * 
 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	
 *  @param <T> The type of {@link Image}  
 *	@created 26 Sep 2011
 */
public interface ShotDetectedListener<T extends Image<?,T>>
{
	/**
	 * 	Called when a shot is detected by the shot detector with the
	 * 	shot boundary definition and an associated key frame.
	 * 
	 *  @param sb The shot boundary definition
	 *  @param vk The keyframe
	 */
	public void shotDetected( ShotBoundary<T> sb, VideoKeyframe<T> vk );
	
	/**
	 * 	Called every time a differential between two frames has been
	 * 	calculated.
	 * 
	 *  @param vt The timecode of the differential frame.
	 *  @param d The differential value
	 *  @param frame The current frame
	 */
	public void differentialCalculated( VideoTimecode vt, double d, T frame );
}
