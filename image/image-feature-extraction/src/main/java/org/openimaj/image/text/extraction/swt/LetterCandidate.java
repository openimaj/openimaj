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
package org.openimaj.image.text.extraction.swt;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.openimaj.image.FImage;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.pixel.PixelSet;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * This class models a candidate textual letter/character from the
 * {@link SWTTextDetector}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class LetterCandidate extends Candidate {
	protected WordCandidate word;
	protected LineCandidate line;
	protected PixelSet cc;
	protected float averageBrightness;
	protected Pixel centroid;
	protected float medianStrokeWidth;

	protected LetterCandidate(PixelSet cc, float medianStrokeWidth, FImage image) {
		this.cc = cc;
		this.medianStrokeWidth = medianStrokeWidth;

		regularBoundingBox = cc.calculateRegularBoundingBox();

		centroid = cc.calculateCentroidPixel();

		averageBrightness = 0;
		for (final Pixel p : cc.pixels) {
			averageBrightness += image.pixels[p.y][p.x];
		}
		averageBrightness /= cc.pixels.size();
	}

	/**
	 * Compute the regular bounding rectangle of the given list of letter
	 * candidates
	 * 
	 * @param letters
	 *            the letter candidates
	 * @return the bounds rectangle
	 */
	public static Rectangle computeBounds(List<LetterCandidate> letters) {
		float minx = Float.MAX_VALUE;
		float miny = Float.MAX_VALUE;
		float maxx = 0;
		float maxy = 0;

		for (final LetterCandidate letter : letters) {
			final Rectangle r = letter.cc.calculateRegularBoundingBox();

			if (r.x < minx)
				minx = r.x;
			if (r.y < miny)
				miny = r.y;
			if (r.x + r.width > maxx)
				maxx = r.x + r.width;
			if (r.y + r.height > maxy)
				maxy = r.y + r.height;
		}

		return new Rectangle(minx, miny, maxx - minx, maxy - miny);
	}

	@Override
	public String toString() {
		return regularBoundingBox.toString();
	}

	/**
	 * Filter the components to find likely letter candidates.
	 * 
	 * @param components
	 *            the components to filter
	 * @param swt
	 *            the swt image
	 * @param image
	 *            the original image
	 * @return the potential letter candidates
	 */
	protected static List<LetterCandidate>
			findLetters(List<ConnectedComponent> components, FImage swt, FImage image, SWTTextDetector.Options options)
	{
		final List<LetterCandidate> output = new ArrayList<LetterCandidate>();

		final DescriptiveStatistics stats = new DescriptiveStatistics();
		for (final ConnectedComponent cc : components) {
			// additional check for small area - speeds processing...
			if (cc.pixels.size() < options.minArea)
				continue;

			computeStats(stats, cc, swt);

			final double mean = stats.getMean();
			final double variance = stats.getVariance();
			final double median = stats.getPercentile(50);

			// test variance of stroke width
			if (variance > options.letterVarianceMean * mean)
				continue;

			final Rectangle bb = cc.calculateRegularBoundingBox();

			// test aspect ratio
			final double aspect = Math.max(bb.width, bb.height) / Math.min(bb.width, bb.height);
			if (aspect > options.maxAspectRatio)
				continue;

			// test diameter
			final float diameter = Math.max(bb.width, bb.height);
			if (diameter / median > options.maxDiameterStrokeRatio)
				continue;

			// check occlusion
			int overlapping = 0;
			for (final ConnectedComponent cc2 : components) {
				if (cc2 == cc)
					continue;
				final Rectangle bb2 = cc2.calculateRegularBoundingBox();
				if (bb2.intersectionArea(bb) > 0)
					overlapping++;
			}
			if (overlapping > options.maxNumOverlappingBoxes)
				continue;

			// check height
			if (bb.height < options.minHeight || bb.height > options.maxHeight)
				continue;

			output.add(new LetterCandidate(cc, (float) median, image));
		}

		return output;
	}

	/**
	 * Compute the stroke statistics of a component.
	 * 
	 * @param stats
	 *            the stats object (will be reset)
	 * @param cc
	 *            the component
	 * @param swt
	 *            the swt image
	 */
	private static void computeStats(DescriptiveStatistics stats, PixelSet cc, FImage swt) {
		stats.clear();
		for (final Pixel p : cc.pixels) {
			stats.addValue(swt.pixels[p.y][p.x]);
		}
	}
}
