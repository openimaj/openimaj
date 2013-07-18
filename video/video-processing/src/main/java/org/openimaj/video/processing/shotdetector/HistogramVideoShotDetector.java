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

import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.image.MBFImage;
import org.openimaj.image.analysis.algorithm.histogram.HistogramAnalyser;
import org.openimaj.math.statistics.distribution.Histogram;
import org.openimaj.video.Video;

/**
 * 	Video shot detector class implemented as a video display listener. This
 * 	means that shots can be detected as the video plays. The class also
 * 	supports direct processing of a video file (with no display).  The default
 * 	shot boundary threshold is 5000 which is an unnormalised value and will
 * 	depend on the frame size.
 * 	<p>
 * 	Only the last keyframe is stored during processing, so if you want to store
 * 	a list of keyframes you must store this list yourself by listening to the
 * 	ShotDetected event which provides a VideoKeyframe which has a timecode
 * 	and an image. Each event will receive the same VideoKeyframe instance
 * 	containing different information. USe VideoKeyframe#clone() to make a copy.
 *
 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *
 *	@created 1 Jun 2011
 */
public class HistogramVideoShotDetector
	extends VideoShotDetector<MBFImage>
{
	/** The previous frame's histogram */
	private Histogram lastHistogram;

	/**
	 * 	If you use this constructor, your timecodes will be messed up
	 * 	unless you call {@link #setFPS(double)} before you process
	 * 	any frames.
	 */
	public HistogramVideoShotDetector()
	{
		this.threshold = 5000;
	}

	/**
	 * 	Constructor that takes the frame rate of the source material.
	 *	@param fps The FPS
	 */
	public HistogramVideoShotDetector( final double fps )
	{
		super( fps );
		this.threshold = 5000;
	}

	/**
	 *	Default constructor takes the video to process.
	 *	@param video The video
	 */
	public HistogramVideoShotDetector( final Video<MBFImage> video )
	{
		super( video );
		this.threshold = 5000;
	}

	/**
	 * 	Constructor that determines whether to display the processing.
	 *	@param video The video
	 *	@param display Whether to display the video while processing
	 */
	public HistogramVideoShotDetector( final Video<MBFImage> video, final boolean display )
	{
		super( video, display );
		this.threshold = 5000;
	}

	/**
	 * 	Checks whether a shot boundary occurred between the given frame
	 * 	and the previous frame, and if so, it will add a shot boundary
	 * 	to the shot boundary list.
	 *
	 *  @param frame The new frame to process.
	 */
	@Override
	protected double getInterframeDistance( final MBFImage frame )
	{
		// Get the histogram for the frame.
		final HistogramAnalyser hp = new HistogramAnalyser( 64 );
		if( frame instanceof MBFImage )
			hp.analyseImage( frame.getBand(0) );
		final Histogram newHisto = hp.getHistogram();

		double dist = 0;

		// If we have a last histogram, compare against it.
		if( this.lastHistogram != null )
			dist = newHisto.compare( this.lastHistogram, DoubleFVComparison.EUCLIDEAN );

		this.lastHistogram = newHisto;

		return dist;
	}
}