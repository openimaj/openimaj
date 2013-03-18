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

import java.util.HashMap;
import java.util.Map;

import org.openimaj.image.MBFImage;
import org.openimaj.video.Video;

/**
 *	A shot detector that uses evidence from a number of shot detectors
 *	to determine if a shot has been detected.
 *	<p>
 *	It runs the shot detection for each detector that is registered with the
 *	class and if it determines that a shot boundary occurred the score is increased
 *	by the weighting value for that detector.  The final score is divided by
 *	the number of detectors to get a probability value for the frame being
 *	a shot boundary. By default the threshold is set to 0.75 - that is, there must
 *	be a 3/4 correlation of shot boundary for it to be considered a shot boundary.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 28 Jan 2013
 *	@version $Author$, $Revision$, $Date$
 */
public class CombiShotDetector extends VideoShotDetector<MBFImage>
{
	/** The detectors to use for evidence gathering */
	private Map<VideoShotDetector<MBFImage>,Double> detectors = null;

	/**
	 * 	If you use this constructor, your timecodes will be messed up
	 * 	unless you call {@link #setFPS(double)} before you process
	 * 	any frames.
	 */
	public CombiShotDetector()
	{
		this.detectors = new HashMap<VideoShotDetector<MBFImage>, Double>();
		this.threshold = 0.75;
	}

	/**
	 *	Default constructor that takes the video to be processed
	 *	@param video The video
	 */
	public CombiShotDetector( final Video<MBFImage> video )
	{
		super( video );
		this.detectors = new HashMap<VideoShotDetector<MBFImage>, Double>();
		this.threshold = 0.75;
	}

	/**
	 * 	Add a shot detector that will be used in the evidence gathering.
	 *	@param detector The detector
	 * 	@param weight The weight to use for this detector
	 */
	public void addVideoShotDetector( final VideoShotDetector<MBFImage> detector,
			final double weight )
	{
		if( weight > 1 || weight < 0 )
			throw new IllegalArgumentException(
					"Detector weight must be between "+
					"0 and 1 inclusive" );
		this.detectors.put( detector, weight );
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.video.processing.shotdetector.VideoShotDetector#getInterframeDistance(org.openimaj.image.Image)
	 */
	@Override
	protected double getInterframeDistance( final MBFImage thisFrame )
	{
		double score = 0;
		for( final VideoShotDetector<MBFImage> detector : this.detectors.keySet() )
		{
			detector.processFrame( thisFrame );
			if( detector.wasLastFrameBoundary() )
				score += this.detectors.get(detector);
		}

		return score/this.detectors.size();
	}
}
