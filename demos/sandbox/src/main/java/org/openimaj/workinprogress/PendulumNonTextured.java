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
package org.openimaj.workinprogress;

import java.io.File;
import java.io.IOException;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processing.transform.FProjectionProcessor;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Circle;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.shape.RotatedRectangle;
import org.openimaj.math.geometry.shape.Triangle;
import org.openimaj.math.geometry.transforms.TransformUtilities;

import Jama.Matrix;

public class PendulumNonTextured {
	public static void main(String[] args) throws IOException {

		// background image
		final FImage background = new FImage(800, 600);

		final Triangle triangle = new Triangle(new Point2dImpl(400, 100),
				new Point2dImpl(395, 500),
				new Point2dImpl(405, 500));

		final FImage pendulumImage = new FImage(800, 600);
		final FImage pendulumMask = new FImage(800, 600);
		for (int y = 0; y < pendulumImage.height; y++) {
			for (int x = 0; x < pendulumImage.width; x++) {
				if (triangle.isInside(new Pixel(x, y))) {
					pendulumImage.pixels[y][x] = 1f;
					pendulumMask.pixels[y][x] = 1;
				}
			}
		}

		final Triangle triangle2 = new Triangle(new Point2dImpl(650, 150),
				new Point2dImpl(645, 250),
				new Point2dImpl(655, 250));

		final FImage clockImage = new FImage(800, 600);
		final FImage clockMask = new FImage(800, 600);
		for (int y = 0; y < clockImage.height; y++) {
			for (int x = 0; x < clockImage.width; x++) {
				if (triangle2.isInside(new Pixel(x, y))) {
					clockImage.pixels[y][x] = 1f;
					clockMask.pixels[y][x] = 1;
				}
			}
		}

		final Circle circle = new Circle(50, 50, 25);
		final FImage linBallImage = new FImage(800, 600);
		final FImage linBallMask = new FImage(800, 600);
		for (int y = 0; y < linBallImage.height; y++) {
			for (int x = 0; x < linBallImage.width; x++) {
				if (circle.isInside(new Pixel(x, y))) {
					linBallImage.pixels[y][x] = 1f;
					linBallMask.pixels[y][x] = 1;
				}
			}
		}

		final Circle circle2 = new Circle(50, 550, 25);
		final FImage accBallImage = new FImage(800, 600);
		final FImage accBallMask = new FImage(800, 600);
		for (int y = 0; y < accBallImage.height; y++) {
			for (int x = 0; x < accBallImage.width; x++) {
				if (circle2.isInside(new Pixel(x, y))) {
					accBallImage.pixels[y][x] = 1f;
					accBallMask.pixels[y][x] = 1;
				}
			}
		}

		final File dir = new File("/Users/jon/pendulum+circle+notexture");
		dir.mkdirs();
		int i = 0;
		final double theta0 = 0.75;
		final double T = 0.1;
		double theta;

		final double triMaxSpeed = theta0 * 400;
		final double clockMaxSpeed = 50 * 100;
		final double linBallMaxSpeed = 3000;
		final double accBallMaxSpeed = 30000;

		final double triMaxAcc = theta0 * 400;
		final double accBallMaxAcc = 500000;

		for (double t = 0; t < 1; t += 0.001, i++) {
			theta = theta0 * Math.cos(2 * Math.PI * t / T);

			final FImage rotPendulumMask = rotate(pendulumMask, theta, 400, 100);
			final FImage rotPendulumImage = rotate(pendulumImage, theta, 400, 100);

			// clock - constant angular motion
			final FImage rotClockMask = rotate(clockMask, t * 50, 650, 150);
			final FImage rotClockImage = rotate(clockImage, t * 50, 650, 150);
			DisplayUtilities.displayName(rotClockMask, "foo");

			// upper circle - linear motion
			final FImage transLinBallMask = translate(linBallMask, (float) t * 3000, 0f);
			final FImage transLinBallImage = translate(linBallImage, (float) t * 3000, 0f);

			// lower circle - accel motion
			final FImage transAccBallMask = translate(accBallMask, (float) (t * t * 500 * 500), 0f);
			final FImage transAccBallImage = translate(accBallImage, (float) (t * t * 500 * 500), 0f);

			final FImage frame = new FImage(800, 600);
			final FImage frameVelX = new FImage(800, 600);
			final FImage frameVelY = new FImage(800, 600);
			final FImage frameVelMag = new FImage(800, 600);
			final FImage frameAccX = new FImage(800, 600);
			final FImage frameAccY = new FImage(800, 600);
			final FImage frameAccMag = new FImage(800, 600);
			frameVelX.fill(0.5f);
			frameVelY.fill(0.5f);
			frameAccX.fill(0.5f);
			frameAccY.fill(0.5f);

			for (int y = 0; y < frame.height; y++) {
				for (int x = 0; x < frame.width; x++) {
					if (rotPendulumMask.pixels[y][x] > 0.5) {
						frame.pixels[y][x] = rotPendulumImage.pixels[y][x];

						// Velocity of the pendulum triangle
						final double dx = x - 400, dy = y - 100, r = Math.sqrt(dx * dx + dy * dy);
						final double vt = -r * theta0 * Math.sin(2 * Math.PI * t / T);
						final double vx = Math.cos(theta) * vt;
						final double vy = Math.sin(theta) * vt;
						frameVelX.pixels[y][x] = (float) ((vx + triMaxSpeed) / (2 * triMaxSpeed));
						frameVelY.pixels[y][x] = (float) ((vy + triMaxSpeed) / (2 * triMaxSpeed));
						frameVelMag.pixels[y][x] = (float) (Math.abs(vt) / (triMaxSpeed));

						// Acceleration of the pendulum triangle
						final double at = -r * theta0 * Math.cos(2 * Math.PI * t / T);
						final double ax = Math.cos(theta) * at;
						final double ay = Math.sin(theta) * at;
						frameAccX.pixels[y][x] = (float) ((ax + triMaxAcc) / (2 * triMaxAcc));
						frameAccY.pixels[y][x] = (float) ((ay + triMaxAcc) / (2 * triMaxAcc));
						frameAccMag.pixels[y][x] = (float) (Math.abs(at) / (triMaxAcc));
					} else if (rotClockMask.pixels[y][x] > 0.5) {
						frame.pixels[y][x] = rotClockImage.pixels[y][x];

						// velocity of the clock triangle
						final double dx = x - 650, dy = y - 150, r = Math.sqrt(dx * dx + dy * dy);
						final double vt = r * 50;
						final double vx = Math.cos(50 * t) * vt;
						final double vy = Math.sin(50 * t) * vt;

						frameVelX.pixels[y][x] = (float) ((vx + clockMaxSpeed) / (2 * clockMaxSpeed));
						frameVelY.pixels[y][x] = (float) ((vy + clockMaxSpeed) / (2 * clockMaxSpeed));
						frameVelMag.pixels[y][x] = (float) (Math.abs(vt) / (clockMaxSpeed));

						// acceleration of the clock triangle
						// !!!clock doesn't accelerate!!!
					} else if (transLinBallMask.pixels[y][x] > 0.5) {
						frame.pixels[y][x] = transLinBallImage.pixels[y][x];

						// velocity of the linear ball
						frameVelX.pixels[y][x] = (float) (3000f / (2 * linBallMaxSpeed));
						frameVelMag.pixels[y][x] = (float) (3000f / linBallMaxSpeed);

						// acceleration of the linear ball
						// !!!ball doesn't accelerate!!!
					} else if (transAccBallMask.pixels[y][x] > 0.5) {
						frame.pixels[y][x] = transAccBallImage.pixels[y][x];

						// velocity of the accelerating ball
						frameVelX.pixels[y][x] = (float) (500000 * t / (2 * accBallMaxSpeed));
						frameVelMag.pixels[y][x] = (float) (500000 * t / accBallMaxSpeed);

						// acceleration of the accelerating ball
						frameAccX.pixels[y][x] = (float) (500000 / (2 * accBallMaxAcc));
						frameAccMag.pixels[y][x] = (float) (500000 / accBallMaxAcc);
					} else {
						frame.pixels[y][x] = background.pixels[y][x];
					}
				}
			}

			frame.drawShapeFilled(new Rectangle(50, 275, 50, 50), 1f);
			frame.drawShapeFilled(new RotatedRectangle(75, 300, 50, 50, Math.PI / 4), 1f);

			DisplayUtilities.displayName(frame, "");
			// DisplayUtilities.displayName(frameVelX, "Vx");
			// DisplayUtilities.displayName(frameVelY, "Vy");
			// DisplayUtilities.displayName(frameVelMag, "Velocity Magnitude");
			// DisplayUtilities.displayName(frameAccX, "Ax");
			// DisplayUtilities.displayName(frameAccY, "Ay");
			// DisplayUtilities.displayName(frameAccMag,
			// "Acceleration Magnitude");

			ImageUtilities.write(frame, new File(dir, "frame_" + i + ".png"));
			// ImageUtilities.write(frameVelX, new File(dir, "frame_vx_" + i +
			// ".png"));
			// ImageUtilities.write(frameVelY, new File(dir, "frame_vy_" + i +
			// ".png"));
			// ImageUtilities.write(frameVelMag, new File(dir, "frame_vm+" + i +
			// ".png"));
			// ImageUtilities.write(frameAccX, new File(dir, "frame_ax_" + i +
			// ".png"));
			// ImageUtilities.write(frameAccY, new File(dir, "frame_ay_" + i +
			// ".png"));
			// ImageUtilities.write(frameAccMag, new File(dir, "frame_am_" + i +
			// ".png"));
		}
	}

	private static FImage rotate(final FImage image, double angle, float px, float py) {
		final Matrix transform = TransformUtilities.rotationMatrixAboutPoint(angle, px, py);
		final FProjectionProcessor pp = new FProjectionProcessor();
		pp.setMatrix(transform);
		pp.accumulate(image);
		return pp.performProjection(true, 0f);
	}

	private static FImage translate(final FImage image, float x, float y) {
		final Matrix transform = TransformUtilities.translateMatrix(x, y);
		final FProjectionProcessor pp = new FProjectionProcessor();
		pp.setMatrix(transform);
		pp.accumulate(image);
		return pp.performProjection(true, 0f);
	}
}
