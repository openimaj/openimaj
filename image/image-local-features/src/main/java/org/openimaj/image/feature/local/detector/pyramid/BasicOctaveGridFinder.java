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
package org.openimaj.image.feature.local.detector.pyramid;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.analysis.pyramid.Octave;
import org.openimaj.image.analysis.pyramid.gaussian.GaussianOctave;
import org.openimaj.image.analysis.pyramid.gaussian.GaussianPyramidOptions;
import org.openimaj.image.processor.SinglebandImageProcessor;

/**
 * An {@link AbstractOctaveInterestPointFinder} that detects points on a regular
 * grid.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <OCTAVE>
 *            Type of underlying {@link Octave}
 * @param <IMAGE>
 *            Type of underlying {@link Image}
 */
public class BasicOctaveGridFinder<OCTAVE extends Octave<?, ?, IMAGE>, IMAGE extends Image<?, IMAGE> & SinglebandImageProcessor.Processable<Float, FImage, IMAGE>>
		extends AbstractOctaveInterestPointFinder<OCTAVE, IMAGE>
{
	int skipX = 50;
	int skipY = 50;
	int borderX = 5;
	int borderY = 5;
	int startScaleIndex = 1;
	int scaleSkip = 1;
	int stopScaleIndex = 2;

	@SuppressWarnings("unchecked")
	@Override
	public void process(OCTAVE octave) {
		this.octave = octave;
		int scales = 0;

		if (octave instanceof GaussianOctave) {
			scales = ((GaussianPyramidOptions<IMAGE>) octave.options).getScales();
		}

		final IMAGE[] images = octave.images;
		final int height = images[0].getHeight();
		final int width = images[0].getWidth();

		// search through the scale-space images, leaving a border
		for (currentScaleIndex = startScaleIndex; currentScaleIndex < stopScaleIndex; currentScaleIndex += scaleSkip) {
			for (int y = borderY; y < height - borderY; y += skipY) {
				for (int x = borderX; x < width - borderX; x += skipX) {
					float octaveScale = currentScaleIndex;

					if (octave instanceof GaussianOctave) {
						octaveScale = ((GaussianPyramidOptions<IMAGE>) octave.options).getInitialSigma()
								* (float) Math.pow(2.0, currentScaleIndex / scales);
					}

					listener.foundInterestPoint(this, x, y, octaveScale);
				}
			}
		}
	}
}
