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
package org.openimaj.image.camera.calibration;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.contour.Contour;
import org.openimaj.image.contour.SuzukiContourProcessor;
import org.openimaj.image.processing.algorithm.FilterSupport;
import org.openimaj.image.processing.algorithm.MinMaxAnalyser;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.math.geometry.shape.RotatedRectangle;
import org.openimaj.util.pair.FloatIntPair;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayAdapter;
import org.openimaj.video.capture.VideoCapture;

/**
 * Analyser for performing a fast check to see if a chessboard is in the input
 * image.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class FastChessboardDetector implements ImageAnalyser<FImage> {
	private static final float BLACK_LEVEL = 20.f / 255f;
	private static final float WHITE_LEVEL = 130.f / 255f;
	private static final float BLACK_WHITE_GAP = 70.f / 255f;

	private static final float MIN_ASPECT_RATIO = 0.3f;
	private static final float MAX_ASPECT_RATIO = 3.0f;
	private static final float MIN_BOX_SIZE = 10.0f;

	private int patternHeight;
	private int patternWidth;
	private boolean result;

	/**
	 * * Construct with the given pattern size
	 * 
	 * @param patternWidth
	 *            the pattern width
	 * @param patternHeight
	 *            the pattern height
	 */
	public FastChessboardDetector(int patternWidth, int patternHeight) {
		this.patternWidth = patternWidth;
		this.patternHeight = patternHeight;
	}

	private void quickThresh(FImage in, FImage out, float thresh, boolean inverse) {
		int low = 0;
		int high = 1;

		if (inverse) {
			low = 1;
			high = 0;
		}

		for (int y = 0; y < in.height; y++) {
			for (int x = 0; x < in.width; x++) {
				out.pixels[y][x] = in.pixels[y][x] > thresh ? low : high;
			}
		}
	}

	@Override
	public void analyseImage(FImage src) {
		final FImage thresh = new FImage(src.width, src.height);

		final MinMaxAnalyser mma = new MinMaxAnalyser(FilterSupport.BLOCK_3x3);
		src.analyseWith(mma);

		final FImage white = mma.min;
		final FImage black = mma.max;

		result = false;
		for (float threshLevel = BLACK_LEVEL; threshLevel < WHITE_LEVEL && !result; threshLevel += (20.0f / 255f))
		{
			final List<FloatIntPair> quads = new ArrayList<FloatIntPair>();

			quickThresh(white, thresh, threshLevel + BLACK_WHITE_GAP, false);
			getQuadrangleHypotheses(SuzukiContourProcessor.findContours(thresh), quads, 1);

			quickThresh(black, thresh, threshLevel, true);
			getQuadrangleHypotheses(SuzukiContourProcessor.findContours(thresh), quads, 0);

			final int minQuadsCount = patternWidth * patternHeight / 2;
			Collections.sort(quads, FloatIntPair.FIRST_ITEM_ASCENDING_COMPARATOR);

			// now check if there are many hypotheses with similar sizes
			// do this by floodfill-style algorithm
			final float sizeRelDev = 0.4f;

			for (int i = 0; i < quads.size(); i++)
			{
				int j = i + 1;
				for (; j < quads.size(); j++)
				{
					if (quads.get(j).first / quads.get(i).first > 1.0f + sizeRelDev)
					{
						break;
					}
				}

				if (j + 1 > minQuadsCount + i)
				{
					// check the number of black and white squares
					final int[] counts = new int[2];
					countClasses(quads, i, j, counts);
					final int blackCount = (int) Math.round(Math.ceil(patternWidth / 2.0)
							* Math.ceil(patternHeight / 2.0));
					final int whiteCount = (int) Math.round(Math.floor(patternWidth / 2.0)
							* Math.floor(patternHeight / 2.0));
					if (counts[0] < blackCount * 0.75 ||
							counts[1] < whiteCount * 0.75)
					{
						continue;
					}
					result = true;
					break;
				}
			}
		}
	}

	void countClasses(List<FloatIntPair> pairs, int idx1, int idx2, int[] counts)
	{
		// counts.assign(2, 0);
		// counts[2] = 0; // why?
		for (int i = idx1; i != idx2; i++)
		{
			counts[pairs.get(i).second]++;
		}
	}

	void getQuadrangleHypotheses(Contour contours, List<FloatIntPair> quads, int classId) {
		for (final Contour seq : contours.contourIterable()) {
			// can assume the the contour is simple, thus making convex hull
			// computation much faster
			final RotatedRectangle box = seq.minimumBoundingRectangle(true);

			final float boxSize = Math.max(box.width, box.height);
			if (boxSize < MIN_BOX_SIZE)
			{
				continue;
			}

			final float aspectRatio = box.width / Math.max(box.height, 1);
			if (aspectRatio < MIN_ASPECT_RATIO || aspectRatio > MAX_ASPECT_RATIO)
			{
				continue;
			}
			quads.add(new FloatIntPair(boxSize, classId));
		}
	}

	/**
	 * Check whether the last image analysed with {@link #analyseImage(FImage)}
	 * was likely to contain a suitable chessboard pattern.
	 * 
	 * @return true if pattern detected; false otherwise
	 */
	public boolean chessboardDetected() {
		return this.result;
	}

	/**
	 * Simple test program
	 * 
	 * @param args
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static void main(String[] args) throws MalformedURLException, IOException {
		final FastChessboardDetector fcd = new FastChessboardDetector(9, 6);
		final VideoDisplay<MBFImage> vd = VideoDisplay.createVideoDisplay(new VideoCapture(640,
				480));
		vd.setCalculateFPS(true);
		vd.addVideoListener(new VideoDisplayAdapter<MBFImage>() {
			@Override
			public void beforeUpdate(MBFImage frame) {
				fcd.analyseImage(frame.flatten());
				frame.drawText(fcd.result + "", 100, 100, HersheyFont.FUTURA_LIGHT,
						20, RGBColour.RED);
				System.out.println(vd.getDisplayFPS());
			}
		});
	}
}
