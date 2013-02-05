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

import java.util.HashMap;
import java.util.Map;

import org.openimaj.image.FImage;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.video.Video;
import org.openimaj.video.VideoFrame;
import org.openimaj.video.VideoSubFrame;

/**
 *	Estimates the motion field over a grid.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 1 Mar 2012
 *
 */
public class GridMotionEstimator extends MotionEstimator
{
	private int x, y;
	private boolean fixed;

	/**
	 * 	Construct a grid-based motion estimator. If <code>fixed</code> is
	 * 	true, the x and y values represent the width and height of the pixel
	 * 	blocks. If <code>fixed</code> is false, the x and y represent the number
	 * 	of grid elements to spread evenly across the frame.
	 *
	 *	@param alg The estimator algorithm to use
	 *	@param x The x value
	 *	@param y The y value
	 *	@param fixed Whether x and y represent pixels or grid count.
	 */
	public GridMotionEstimator( MotionEstimatorAlgorithm alg,
			int x, int y, boolean fixed )
	{
		super( alg );
		this.x = x; this.y = y;
		this.fixed = fixed;
	}

	/**
	 * 	Construct a chained grid-based motion estimator. If <code>fixed</code> is
	 * 	true, the x and y values represent the width and height of the pixel
	 * 	blocks. If <code>fixed</code> is false, the x and y represent the number
	 * 	of grid elements to spread evenly across the frame.
	 *
	 *	@param v The video to chain to
	 *	@param alg The estimator algorithm to use
	 *	@param x The x value
	 *	@param y The y value
	 *	@param fixed Whether x and y represent pixels or grid count.
	 */
	public GridMotionEstimator( Video<FImage> v, MotionEstimatorAlgorithm alg,
			int x, int y, boolean fixed )
	{
		super( v, alg );
		this.x = x; this.y = y;
		this.fixed = fixed;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.video.analysis.motion.MotionEstimator#estimateMotionField(org.openimaj.video.analysis.motion.MotionEstimator.MotionEstimatorAlgorithm, org.openimaj.image.FImage, org.openimaj.image.FImage[])
	 */
	@Override
	protected Map<Point2d, Point2d> estimateMotionField(
			MotionEstimatorAlgorithm estimator, VideoFrame<FImage> vf,
			VideoFrame<FImage>[] array )
	{
		if( array.length < 1 )
			return new HashMap<Point2d,Point2d>();

		int gw = 0, gh = 0;
		if( fixed )
		{
			gw = x;
			gh = y;
		}
		else
		{
			gw = vf.frame.getWidth()/x;
			gh = vf.frame.getHeight()/y;
		}

		Map<Point2d,Point2d> out = new HashMap<Point2d, Point2d>();

		@SuppressWarnings( "unchecked" )
		VideoSubFrame<FImage>[] otherFrames = new VideoSubFrame[array.length];

		for( int yy = 0; yy < vf.frame.getHeight(); yy += gh )
		{
			for( int xx = 0; xx < vf.frame.getWidth(); xx += gw )
			{
				for( int ff = 0; ff < array.length; ff++ )
					otherFrames[ff] = new VideoSubFrame<FImage>(
							array[ff].frame,
							array[ff].timecode,
							new Rectangle(xx, yy, gw, gh));

				// vf.frame.drawShape( new Rectangle(xx,yy,gw,gh), 1, 0f );

				out.put( new Point2dImpl(xx+gw/2f,yy+gh/2f),
						estimator.estimateMotion( new VideoSubFrame<FImage>(
							vf.frame,
							vf.timecode,
							new Rectangle(xx, yy, gw, gh)),
							otherFrames ) );
			}
		}

		return out;
	}
}
