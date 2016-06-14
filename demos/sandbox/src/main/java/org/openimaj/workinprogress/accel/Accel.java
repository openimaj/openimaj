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
package org.openimaj.workinprogress.accel;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.video.ArrayBackedVideo;
import org.openimaj.video.processing.motion.GridMotionEstimator;
import org.openimaj.video.processing.motion.MotionEstimator;
import org.openimaj.video.processing.motion.MotionEstimatorAlgorithm;

public class Accel {
	public static void main(String[] args) throws IOException, InterruptedException {
		final FImage[] sequence = new FImage[3];
		for (int i = 0; i < sequence.length; i++) {
			sequence[i] = ImageUtilities.readF(new File("/Users/jon/pendulum+circle/frame_" + (i + 10) + ".png"));
		}

		final MotionEstimatorAlgorithm.TEMPLATE_MATCH alg = new MotionEstimatorAlgorithm.TEMPLATE_MATCH();
		final MotionEstimator e = new GridMotionEstimator(new ArrayBackedVideo<FImage>(sequence),
				alg, 10, 10, true);

		e.getNextFrame();
		e.getNextFrame();
		final Map<Point2d, Point2d> mv1 = e.getMotionVectors();
		e.getNextFrame();
		final Map<Point2d, Point2d> mv2 = e.getMotionVectors();

		drawVectors(sequence, mv1);
		drawVectors(sequence, mv2);

		final Map<Point2d, Point2d> accel = new HashMap<Point2d, Point2d>();
		for (final Entry<Point2d, Point2d> p : mv1.entrySet()) {
			final Point2d from = p.getKey();
			final Point2d to = p.getValue().copy();
			to.translate(from);

			final Point2d v1 = p.getValue();
			Point2d v2 = mv2.get(to);
			if (v2 == null)
				v2 = new Point2dImpl(0, 0);

			final Point2d acc = v2.copy().minus(v1);
			accel.put(to, acc);
		}

		drawVectors(sequence, accel);
	}

	private static void drawVectors(final FImage[] sequence, final Map<Point2d, Point2d> mv1) {
		final FImage fr = new FImage(sequence[0].width, sequence[0].height);
		for (final Entry<Point2d, Point2d> p : mv1.entrySet()) {
			final Point2d from = p.getKey();
			final Point2d to = p.getValue().copy();
			to.translate(from);

			if (!from.equals(to))
				fr.drawLine(from, to, 1f);
		}
		DisplayUtilities.display(fr);
	}
}
