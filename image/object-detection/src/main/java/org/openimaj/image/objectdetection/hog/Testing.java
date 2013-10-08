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

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.objectdetection.filtering.OpenCVGrouping;
import org.openimaj.io.IOUtils;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.pair.ObjectIntPair;

public class Testing {
	public static void main(String[] args) throws IOException {
		final HOGClassifier classifier = IOUtils.readFromFile(new File("final-classifier.dat"));

		final HOGDetector detector = new HOGDetector(classifier);
		final FImage img = ImageUtilities.readF(new File("/Users/jsh2/Data/INRIAPerson/Test/pos/crop_000006.png"));
		final List<Rectangle> dets = detector.detect(img);

		final List<ObjectIntPair<Rectangle>> fdets = new OpenCVGrouping().apply(dets);

		final MBFImage rgb = img.toRGB();
		for (final Rectangle r : dets) {
			rgb.drawShape(r, RGBColour.RED);
		}
		for (final ObjectIntPair<Rectangle> r : fdets) {
			rgb.drawShape(r.first, RGBColour.GREEN);
		}

		DisplayUtilities.display(rgb);

		// classifier.prepare(img);
		//
		// for (int k = 0; k < 1000; k++) {
		// final Timer t1 = Timer.timer();
		// int width = 64;
		// int height = 128;
		// int step = 8;
		// for (int level = 0; level < 10; level++) {
		// final MBFImage rgb = img.toRGB();
		// final Timer t2 = Timer.timer();
		// int nwindows = 0;
		// for (int y = 0; y < img.height - height; y += step) {
		// for (int x = 0; x < img.width - width; x += step) {
		// final Rectangle rectangle = new Rectangle(x, y, width, height);
		// nwindows++;
		// if (classifier.classify(rectangle)) {
		// rgb.drawShape(new Rectangle(x, y, width, height), RGBColour.RED);
		// }
		// }
		// }
		// System.out.format("Image %d x %d (%d windows) took %2.2fs\n",
		// img.width, img.height, nwindows,
		// t2.duration() / 1000.0);
		// DisplayUtilities.displayName(rgb, "name " + level);
		//
		// width = (int) Math.floor(width * 1.2);
		// height = (int) Math.floor(height * 1.2);
		// step = (int) Math.floor(step * 1.2);
		// }
		// System.out.format("Total time: %2.2fs\n", t1.duration() / 1000.0);
		// }
	}
}
