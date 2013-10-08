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
package org.openimaj.demos.sandbox.image.text;

import java.util.List;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.openimaj.image.FImage;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.math.geometry.shape.Rectangle;

public class LetterCandidate {
	ConnectedComponent cc;
	float averageBrightness;
	Pixel centroid;
	float medianStrokeWidth;
	Rectangle regularBoundingBox;

	public LetterCandidate(ConnectedComponent cc, float medianStrokeWidth, FImage image) {
		this.cc = cc;
		this.medianStrokeWidth = medianStrokeWidth;

		regularBoundingBox = cc.calculateRegularBoundingBox();

		centroid = cc.calculateCentroidPixel();

		final DescriptiveStatistics ds = new DescriptiveStatistics(cc.pixels.size());
		for (final Pixel p : cc.pixels) {
			ds.addValue(image.pixels[p.y][p.x]);
		}
		averageBrightness = (float) ds.getMean();
	}

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
}
