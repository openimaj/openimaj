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

import java.util.Arrays;
import java.util.Random;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.math.geometry.shape.Circle;
import org.openimaj.math.matrix.MatrixUtils;
import org.openimaj.math.statistics.distribution.SphericalMultivariateGaussian;
import org.openimaj.ml.linear.projection.LargeMarginDimensionalityReduction;

public class LMDR {
	public static void main(String[] args) {
		final SphericalMultivariateGaussian g1 = new SphericalMultivariateGaussian(10);
		g1.variance = 8;
		g1.mean.getArray()[0][0] = 10;

		final SphericalMultivariateGaussian g2 = new SphericalMultivariateGaussian(10);
		g2.variance = 8;
		g2.mean.getArray()[0][1] = 10;

		final SphericalMultivariateGaussian g3 = new SphericalMultivariateGaussian(10);
		g3.variance = 8;
		g3.mean.getArray()[0][2] = 10;

		final Random rng = new Random();

		final double[][] data = new double[150][];
		for (int i = 0; i < data.length; i++) {
			if (i % 3 == 0) {
				data[i] = g1.sample(rng);
			} else if (i % 3 == 1) {
				data[i] = g2.sample(rng);
			} else {
				data[i] = g3.sample(rng);
			}
		}

		final LargeMarginDimensionalityReduction lmdr = new LargeMarginDimensionalityReduction(2);
		lmdr.initialise(data);

		System.out.println(MatrixUtils.toMatlabString(lmdr.W));
		System.out.println(lmdr.b);
		drawAllPoints(lmdr, data);

		int iter = 0;
		for (int o = 0; o < 10; o++) {
			for (int i = 0; i < data.length; i++) {
				for (int j = i + 1; j < data.length; j++) {
					if (lmdr.step(data[i], data[j], i % 3 == j % 3)) {
						System.out.println(iter);
						drawAllPoints(lmdr, data);
					}
					iter++;
				}
			}
		}
		System.out.println("done " + iter);
	}

	private static void drawAllPoints(LargeMarginDimensionalityReduction lmdr, double[][] data) {
		final MBFImage img = new MBFImage(500, 500, ColourSpace.RGB);

		for (int i = 0; i < data.length; i++) {
			final double[] pto = lmdr.project(data[i]);

			System.out.println(i % 2 + " " + Arrays.toString(pto));

			img.drawShapeFilled(new Circle((float) pto[0] + img.getWidth() / 2, (float) pto[1] + img.getHeight() / 2, 3),
					i % 3 == 0 ? RGBColour.RED : i % 3 == 1 ? RGBColour.GREEN : RGBColour.BLUE);
		}
		DisplayUtilities.displayName(img, "");
		try {
			Thread.sleep(200);
		} catch (final InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
