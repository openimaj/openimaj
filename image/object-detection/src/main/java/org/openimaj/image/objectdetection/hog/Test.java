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
package org.openimaj.image.objectdetection.hog;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.feature.dense.gradient.HOG;
import org.openimaj.image.feature.dense.gradient.binning.FlexibleHOGStrategy;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.statistics.distribution.Histogram;
import org.openimaj.util.pair.ObjectDoublePair;

public class Test {
	public static void main(String[] args) throws IOException {
		final FImage img = ImageUtilities.readF(new URL(
				"http://www.di.ens.fr/willow/teaching/recvis10/final_project/detection/car-img1.png"));

		final HOG h = new HOG(new FlexibleHOGStrategy(8, 8, 2));
		h.analyseImage(img);

		final Rectangle r = new Rectangle(47, 92, 30, 30);
		final Histogram f = h.getFeatureVector(r).clone();

		img.drawShape(r, 1f);
		DisplayUtilities.display(img);

		final FImage img2 = ImageUtilities.readF(new URL(
				"http://www.di.ens.fr/willow/teaching/recvis10/final_project/detection/car-img3.png"));
		h.analyseImage(img2);

		final List<ObjectDoublePair<Rectangle>> data = new ArrayList<ObjectDoublePair<Rectangle>>();
		for (int y = 0; y < img2.height - 30; y++) {
			for (int x = 0; x < img2.width - 30; x++) {
				final Rectangle rr = new Rectangle(x, y, 30, 30);
				final Histogram ff = h.getFeatureVector(rr);

				final double c = DoubleFVComparison.EUCLIDEAN.compare(f, ff);
				data.add(ObjectDoublePair.pair(rr, c));
			}
		}

		Collections.sort(data, new Comparator<ObjectDoublePair<Rectangle>>() {
			@Override
			public int compare(ObjectDoublePair<Rectangle> o1, ObjectDoublePair<Rectangle> o2) {
				return Double.compare(o1.second, o2.second);
			}
		});

		for (int i = 0; i < 10; i++) {
			img2.drawShape(data.get(i).first, 1F);
		}
		DisplayUtilities.display(img2);
	}
}
