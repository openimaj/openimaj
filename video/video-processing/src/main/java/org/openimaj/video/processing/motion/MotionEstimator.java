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
package org.openimaj.video.processing.motion;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.openimaj.image.FImage;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.video.Video;
import org.openimaj.video.VideoFrame;
import org.openimaj.video.analyser.VideoAnalyser;
import org.openimaj.video.timecode.HrsMinSecFrameTimecode;


/**
 *	A motion estimator will estimate the motion of parts of a video frame.
 *	This class includes a set of algorithms for calculating the motion estimation.
 *	<p>
 *	This class deals with the buffering of frames from the video which to pass
 *	to the motion estimation. The class is abstract and the method
 *	{@link #estimateMotionField(MotionEstimatorAlgorithm, VideoFrame, VideoFrame[])}
 *	must be overridden by an implementing class to provide the field over which
 *	the motion estimation will take place. This field may, for example, be a grid
 *	or an overlapping grid. This overridden method must also determine the appropriate
 *	way to call the motion estimation algorithm while returning a map which maps
 *	a point to a displacement vector.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 1 Mar 2012
 *
 */
@SuppressWarnings( "javadoc" )
public abstract class MotionEstimator extends VideoAnalyser<FImage>
{
	/** The estimator to use */
	private MotionEstimatorAlgorithm estimator = null;

	/** The old frame stack. It's a queue so the oldest frame is popped off */
	private Queue<VideoFrame<FImage>> oldFrames = null;

	/** The estimated motion vectors for the last analysed frame */
	public Map<Point2d,Point2d> motionVectors = null;

	/**
	 * 	Constructor a new motion estimator using the given algorithm.
	 *	@param alg The algorithm to use to estimate motion.
	 */
	public MotionEstimator( MotionEstimatorAlgorithm alg )
	{
		this.estimator = alg;
		oldFrames = new LinkedList<VideoFrame<FImage>>();
	}

	/**
	 * 	Create a chainable motion estimator.
	 *	@param v The video to chain to
	 *	@param alg The algorithm to use to estimate motion
	 */
	public MotionEstimator( Video<FImage> v, MotionEstimatorAlgorithm alg )
	{
		super(v);
		this.estimator = alg;
		oldFrames = new LinkedList<VideoFrame<FImage>>();
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.video.analyser.VideoAnalyser#analyseFrame(org.openimaj.image.Image)
	 */
	@SuppressWarnings( "unchecked" )
	@Override
	public void analyseFrame( FImage frame )
	{
		VideoFrame<FImage> vf = new VideoFrame<FImage>( frame,
				new HrsMinSecFrameTimecode( getTimeStamp(), getFPS() ) );

		motionVectors = estimateMotionField( estimator, vf,
				oldFrames.toArray( new VideoFrame[0] ) );

		oldFrames.offer( vf );

		// Make sure there's never too many frames in the queue
		if( oldFrames.size() > estimator.requiredNumberOfFrames() )
			oldFrames.poll();
	}

	/**
	 * 	Return the estimated motion vectors for the last processed frame.
	 *	@return The estimated motion vectors
	 */
	public Map<Point2d,Point2d> getMotionVectors()
	{
		return motionVectors;
	}

	/**
	 * 	This method needs to be overridden for specific layouts of motion
	 * 	field within the image.
	 *
	 *	@param frame The current frame
	 *	@param array The list of previous frames (based on the estimator)
	 *	@return The motion field
	 */
	protected abstract Map<Point2d, Point2d> estimateMotionField(
			MotionEstimatorAlgorithm estimator, VideoFrame<FImage> frame,
			VideoFrame<FImage>[] array );
}