package org.openimaj.demos.sandbox;

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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JFrame;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.io.IOUtils;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.ml.annotation.bayes.NaiveBayesAnnotator;
import org.openimaj.video.Video;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.processing.motion.GridMotionEstimator;
import org.openimaj.video.processing.motion.MotionEstimator;
import org.openimaj.video.processing.motion.MotionEstimatorAlgorithm;
import org.openimaj.video.translator.MBFImageToFImageVideoTranslator;

public class OpticFlowPlayground {
	public static Direction direction = Direction.NONE;

	public static void main(String[] args) throws FileNotFoundException, IOException {
		// Video<FImage> capture = new MBFImageToFImageVideoTranslator(new
		// VideoCapture(160,120));
		final Video<FImage> capture = new MBFImageToFImageVideoTranslator(new VideoCapture(640, 480));
		final MotionEstimatorAlgorithm.TEMPLATE_MATCH alg = new MotionEstimatorAlgorithm.TEMPLATE_MATCH();
		final MotionEstimator e = new GridMotionEstimator(capture,
				// new MotionEstimatorAlgorithm.PHASE_CORRELATION(),
				alg,
				30, 30, true);
		final NaiveBayesAnnotator<Double, Direction> dirAnn = IOUtils.read(new DataInputStream(new FileInputStream(
				"/Users/ss/.rhino/opticflowann")));

		boolean first = true;
		for (final FImage fImage : e) {
			if (first) {
				first = false;
				continue;
			}
			final Point2dImpl meanMotion = new Point2dImpl(0, 0);
			final Map<Point2d, Point2d> analysis = e.getMotionVectors();
			for (final Entry<Point2d, Point2d> line : analysis.entrySet()) {
				final Point2d to = line.getKey().copy();
				to.translate(line.getValue());
				fImage.drawLine(line.getKey(), to, 1f);
				fImage.drawPoint(line.getKey(), 1f, 3);
				meanMotion.x += line.getValue().getX();
			}
			meanMotion.x /= analysis.size();
			meanMotion.y /= analysis.size();
			final JFrame f = DisplayUtilities.displayName(fImage, "frame");

			f.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if (e.getKeyChar() == 'l') {
						direction = Direction.LEFT;
					}
					else if (e.getKeyChar() == 'r') {
						direction = Direction.RIGHT;
					}
					else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
						direction = Direction.MIDDLE;
					}
				}

				@Override
				public void keyReleased(KeyEvent e) {
					direction = Direction.NONE;
				}
			});
			if (!(direction == Direction.NONE)) {
				System.out.println(String.format("x: %2.2f,%s", meanMotion.x, direction));
				dirAnn.train(new DirectionScore(meanMotion.x, direction));
			} else {
				// final Iterator<Direction> iterator = dirAnn.classify((double)
				// meanMotion.x).getPredictedClasses()
				// .iterator();
				// // if (iterator.hasNext()) {
				// final Direction dir = iterator.next();
				// // System.out.println("Current flow: " + dir);
				// }
			}
		}
	}
}
