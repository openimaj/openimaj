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
package org.openimaj.demos.features;

import javax.swing.JFrame;

import org.openimaj.demos.Demo;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.feature.local.interest.HarrisIPD;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Ellipse;
import org.openimaj.math.geometry.shape.EllipseUtilities;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.transforms.TransformUtilities;

import Jama.Matrix;

/**
 * Demo showing harris points
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
@Demo(
		author = "Sina Samangooei",
		description = "Shows Harris interest point detection on an animated shape",
		keywords = {
				"sift", "animation", "feature-selection" },
		title = "Animated Interest Point Visualiser")
public class AnimatedInterestPointVisualiser {
	private Rectangle rectangle;
	private Point2dImpl point;
	private Matrix transform;
	private MBFImage image;
	private int derivscale;
	private HarrisIPD ipd;
	private Ellipse ellipseToDraw;
	private JFrame jframe;
	private float rotation;

	/**
	 * Construct the demo
	 */
	public AnimatedInterestPointVisualiser() {
		this.rectangle = new Rectangle(100, 100, 200, 200);
		this.point = new Point2dImpl(110, 110);
		this.rotation = 0f;
		this.transform = TransformUtilities.rotationMatrixAboutPoint(this.rotation, 200, 200);
		derivscale = 1;
		ipd = new HarrisIPD(derivscale, 3);
		this.image = new MBFImage(400, 400, ColourSpace.RGB);
		this.jframe = DisplayUtilities.display(this.image);
		drawShape();
		updateEllipse();

		class Updater implements Runnable {
			private AnimatedInterestPointVisualiser frame;

			Updater(AnimatedInterestPointVisualiser frame) {
				this.frame = frame;
			}

			@Override
			public void run() {
				while (true) {
					frame.drawShape();
					frame.updateEllipse();
					frame.drawFrame();
					frame.updateTransform();
					try {
						Thread.sleep(1000 / 30);
					} catch (final InterruptedException e) {
					}
				}
			}
		}
		final Thread t = new Thread(new Updater(this));
		t.start();
	}

	/**
	 * Update the transform
	 */
	public void updateTransform() {
		this.rotation += Math.PI / 100f;
		this.transform = TransformUtilities.rotationMatrixAboutPoint(
				this.rotation, 200, 200);
	}

	/**
	 * Draw the frame
	 */
	public void drawFrame() {
		this.image.createRenderer()
				.drawShape(this.ellipseToDraw, RGBColour.RED);
		DisplayUtilities.display(this.image, this.jframe);
	}

	private void updateEllipse() {
		ipd.findInterestPoints(Transforms.calculateIntensityNTSC(this.image));
		final Point2dImpl np = this.point.transform(transform);
		final Matrix sm = ipd.getSecondMomentsAt((int) np.x, (int) np.y);
		ellipseToDraw = EllipseUtilities.ellipseFromSecondMoments(np.x, np.y,
				sm, 5 * 2);
	}

	/**
	 * Draw the shape
	 */
	public void drawShape() {
		this.image.fill(RGBColour.WHITE);
		this.image.createRenderer().drawShapeFilled(
				this.rectangle.transform(transform), RGBColour.BLACK);

		this.image = image.process(new FGaussianConvolve(5));
		this.image.createRenderer().drawPoint(this.point.transform(transform),
				RGBColour.RED, 1);
	}

	/**
	 * The main method
	 *
	 * @param args
	 *            ignored
	 */
	public static void main(String args[]) {
		new AnimatedInterestPointVisualiser();
	}
}
