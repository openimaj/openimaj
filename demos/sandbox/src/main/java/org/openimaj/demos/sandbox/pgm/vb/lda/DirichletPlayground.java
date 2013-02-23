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
package org.openimaj.demos.sandbox.pgm.vb.lda;

import gov.sandia.cognition.math.matrix.mtj.DenseVector;
import gov.sandia.cognition.math.matrix.mtj.DenseVectorFactoryMTJ;
import gov.sandia.cognition.statistics.distribution.DirichletDistribution;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Triangle;

public class DirichletPlayground {
	private double eta = 0.00001f;
	private DirichletDistribution dir;

	public DirichletPlayground() {

	}

	public void render(double alpha) {
		this.dir = new DirichletDistribution(3);
		final DenseVector params = new DenseVectorFactoryMTJ().copyArray(new double[] { alpha, alpha, alpha });
		dir.convertFromVector(params);

		final MBFImage out = new MBFImage(400, 400, 3);
		final Point2dImpl p1 = new Point2dImpl(out.getWidth() / 2f, 0f);
		final Point2dImpl p2 = new Point2dImpl(out.getWidth(), out.getHeight());
		final Point2dImpl p3 = new Point2dImpl(0, out.getHeight());
		final Triangle simplex = new Triangle(
				p1,
				p2,
				p3
				);
		out.fill(RGBColour.WHITE);
		out.drawShape(simplex, 3, RGBColour.BLACK);
		// DenseVector simplexPoint = new DenseVectorFactoryMTJ().copyArray(new
		// double[]{1/3f,1/3f ,1/3f});
		final double max = 10;
		for (int y = 0; y < out.getWidth(); y++) {
			for (int x = 0; x < out.getHeight(); x++) {
				if (simplex.isInside(new Point2dImpl(x, y))) {
					double p1d = Line2d.distance(x, y, p1.x, p1.y) + eta;
					double p2d = Line2d.distance(x, y, p2.x, p2.y) + eta;
					double p3d = Line2d.distance(x, y, p3.x, p3.y) + eta;
					final double sum = p1d + p2d + p3d;
					p1d /= sum;
					p2d /= sum;
					p3d /= sum;
					final DenseVector v = new DenseVectorFactoryMTJ().copyArray(new double[] { p1d, p2d, p3d });
					final double val = Math.min(max, dir.getProbabilityFunction().evaluate(v)) / max;
					out.setPixel(x, y, blend(RGBColour.BLACK, RGBColour.RED, (float) val));
				}
			}
		}
		DisplayUtilities.display(out);
	}

	private Float[] blend(Float[] black, Float[] red, float val) {
		return new Float[] {
				((black[0] * (1 - val)) + (red[0] * (val))),
				((black[1] * (1 - val)) + (red[1] * (val))),
				((black[2] * (1 - val)) + (red[2] * (val))),
		};
	}

	public static void main(String[] args) {
		final DirichletPlayground pg = new DirichletPlayground();
		pg.render(0.3f);
	}
}
