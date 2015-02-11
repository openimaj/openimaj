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
package org.openimaj.video.processing.pixels;

import org.openimaj.image.FImage;
import org.openimaj.video.analyser.VideoAnalyser;
import org.openimaj.video.processing.shotdetector.HistogramVideoShotDetector;

/**
 * Compute the mean and variance fields from a video of {@link FImage} frames.
 * The generated fields could be used to analyse which parts of a video are
 * stationary or change a lot. If your video consists of multiple shots, between
 * which there are large changes in the content, then it probably makes sense to
 * segment the video using a {@link HistogramVideoShotDetector} and apply a new
 * analyser to each shot independently.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class FMeanVarianceField
extends
		VideoAnalyser<FImage>
{
	private FImage mean;
	private FImage m2;
	private int n;

	@Override
	public void analyseFrame(FImage frame) {
		final int width = frame.width;
		final int height = frame.height;

		if (mean == null || mean.width != width || mean.height != height) {
			n = 0;
			mean = new FImage(width, height);
			m2 = new FImage(width, height);
		}

		final float[][] mp = mean.pixels;
		final float[][] m2p = m2.pixels;
		final float[][] fp = frame.pixels;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				final float v = fp[y][x];
				final float delta = v - mp[y][x];

				n++;
				mp[y][x] = mp[y][x] + delta / n;
				m2p[y][x] = m2p[y][x] + delta * (v - mp[y][x]);
			}
		}
	}

	/**
	 * Reset the accumulated field data.
	 *
	 * @see org.openimaj.video.processor.VideoProcessor#reset()
	 */
	@Override
	public void reset() {
		this.mean = null;
		this.m2 = null;
	}

	/**
	 * Get the mean field of all the frames that have been analysed so far.
	 *
	 * @return the mean field.
	 */
	public FImage getMean() {
		return mean;
	}

	/**
	 * Get the variance field of all the frames that have been analysed so far.
	 *
	 * @return the variance field.
	 */
	public FImage getVariance() {
		if (m2 == null)
			return null;

		return m2.divide((float) (n - 1));
	}
}
