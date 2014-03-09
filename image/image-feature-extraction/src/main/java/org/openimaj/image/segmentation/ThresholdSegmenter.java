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
package org.openimaj.image.segmentation;

import gnu.trove.map.hash.TFloatObjectHashMap;

import java.util.Arrays;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.pixel.PixelSet;
import org.openimaj.image.processor.Processor;

/**
 * Simple wrapper to make thresholding algorithms into {@link Segmenter}s by
 * applying the thresholding operation and then gathering the pixel sets
 * belonging to each segment. Note that class does not perform connected
 * component analysis, and for example in the case of binary thresholding, there
 * will only be two {@link PixelSet}s produced (i.e. foreground and background).
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class ThresholdSegmenter implements Segmenter<FImage> {
	Processor<FImage> thresholder;

	/**
	 * Construct with the given thresholding algorithm implementation.
	 * 
	 * @param thresholder
	 *            the thresholding algorithm
	 */
	public ThresholdSegmenter(Processor<FImage> thresholder) {
		this.thresholder = thresholder;
	}

	@Override
	public List<? extends PixelSet> segment(FImage image) {
		final FImage timg = image.process(thresholder);
		final TFloatObjectHashMap<PixelSet> sets = new TFloatObjectHashMap<PixelSet>();

		for (int y = 0; y < timg.height; y++) {
			for (int x = 0; x < timg.width; x++) {
				final float p = image.getPixel(x, y);

				PixelSet ps = sets.get(p);
				if (ps == null)
					sets.put(p, ps = new PixelSet());
				ps.addPixel(x, y);
			}
		}

		return Arrays.asList(sets.values());
	}
}
