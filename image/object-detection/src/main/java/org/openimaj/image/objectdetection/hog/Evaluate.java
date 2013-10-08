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

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.VFSListDataset;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.io.IOUtils;
import org.openimaj.math.geometry.shape.Rectangle;

public class Evaluate {
	public static void main(String[] args) throws IOException {
		final HOGClassifier classifier = IOUtils.readFromFile(new File("initial-classifier.dat"));
		final HOGDetector detector = new HOGDetector(classifier);

		for (float thresh = 0; thresh < 1; thresh += 0.1) {
			detector.threshold = thresh;

			final ListDataset<FImage> neg = new VFSListDataset<FImage>("/Users/jsh2/Data/INRIAPerson/Test/neg",
					ImageUtilities.FIMAGE_READER);
			final ListDataset<FImage> pos = new VFSListDataset<FImage>("/Users/jsh2/Data/INRIAPerson/Test/pos",
					ImageUtilities.FIMAGE_READER);

			int falsePositives = 0;
			int trueNegatives = 0;
			for (final FImage i : neg) {
				final List<Rectangle> rectangles = detector.detect(i);
				if (rectangles.size() > 0)
					falsePositives++;
				else
					trueNegatives++;
			}

			int falseNegatives = 0;
			int truePositives = 0;
			for (final FImage i : pos) {
				final List<Rectangle> rectangles = detector.detect(i);
				if (rectangles.size() > 0)
					truePositives++;
				else
					falseNegatives++;
			}

			final double missRate = (double) falseNegatives / (double) (truePositives + falseNegatives);
			System.out.format("%f\t%d\t%d\t%d\t%d\t%f\n", thresh, truePositives, trueNegatives, falsePositives,
					falseNegatives, missRate);
		}
	}
}
