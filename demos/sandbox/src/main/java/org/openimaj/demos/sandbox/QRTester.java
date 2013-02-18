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
package org.openimaj.demos.sandbox;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

import java.io.IOException;
import java.net.MalformedURLException;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.threshold.OtsuThreshold;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;

public class QRTester {
	public static void main(String[] args) throws MalformedURLException, IOException {
		// MBFImage cimg = ImageUtilities.readMBF(new
		// URL("http://cdn.socialnomics.net/wp-content/uploads/2011/03/QR-code-girl1.jpg"));
		// // MBFImage cimg = ImageUtilities.readMBF(new
		// URL("http://thinkd2c.files.wordpress.com/2011/05/qrcode_wwd.png"));
		// findMarkers(cimg);
		// DisplayUtilities.display(cimg);

		final VideoDisplay<MBFImage> display = VideoDisplay.createVideoDisplay(new VideoCapture(640, 480));
		display.addVideoListener(new VideoDisplayListener<MBFImage>() {

			@Override
			public void beforeUpdate(MBFImage frame) {
				findMarkers(frame);
			}

			@Override
			public void afterUpdate(VideoDisplay<MBFImage> display) {
				// TODO Auto-generated method stub

			}
		});
	}

	static void findMarkers(MBFImage cimg) {
		FImage image = cimg.flatten();
		image = image.processInplace(new OtsuThreshold());
		// image = image.threshold(0.2f);

		for (int y = 0; y < image.height; y += 2) {
			final TIntArrayList centres = processLineH(image, y);

			for (final int x : centres.toArray()) {
				cimg.drawLine(x, y - 10, x, y + 10, RGBColour.RED);
				cimg.drawLine(x - 10, y, x + 10, y, RGBColour.RED);
			}
		}
		// cimg.internalAssign(new MBFImage(image,image,image));
	}

	static TIntArrayList processLineH(FImage image, int y) {
		final TIntArrayList counts = new TIntArrayList();

		int start = 0;
		while (start < image.width) {
			if (image.pixels[y][start] == 0)
				break;
			start++;
		}

		for (int i = start; i < image.width; i++) {
			int count = 0;
			final float state = image.pixels[y][i];
			for (; i < image.width; i++) {
				if (image.pixels[y][i] != state) {
					i--; // step back because the outer loop increments
					break;
				}
				count++;
			}
			counts.add(count);
		}

		return findPossibleH(counts, start);
	}

	static TIntArrayList findPossibleH(TIntArrayList counts, final int start) {
		final TIntArrayList centers = new TIntArrayList();

		// assume first count is black. Only need check patterns starting with
		// black...
		for (int i = 0, co = start; i < counts.size() - 5; i += 2) {
			final TIntList pattern = counts.subList(i, i + 5);

			if (isValid(pattern)) {
				int sum = 0;
				for (final int j : pattern.toArray())
					sum += j;

				centers.add(co + (sum / 2));
			}
			co += counts.get(i) + counts.get(i + 1);
		}
		return centers;
	}

	private static boolean isValid(TIntList pattern) {
		// 1 1 3 1 1
		// B W B W B

		final float[] apat = { 1, 1, 3, 1, 1 };
		final float[] fpat = { pattern.get(0), pattern.get(1), pattern.get(2), pattern.get(3), pattern.get(4) };

		// System.out.print(Arrays.toString(fpat) + "\t\t");

		final float ratio = 4 / (fpat[0] + fpat[1] + fpat[3] + fpat[4]);
		for (int i = 0; i < 5; i++)
			fpat[i] *= ratio;

		float error = 0;
		for (int i = 0; i < 5; i++) {
			final float diff = apat[i] - fpat[i];
			error += diff * diff;
		}

		// System.out.println(error);
		// System.out.println(Arrays.toString(fpat) + "\t\t" + error);
		if (error < 0.5)
			return true;

		return false;
	}

}
