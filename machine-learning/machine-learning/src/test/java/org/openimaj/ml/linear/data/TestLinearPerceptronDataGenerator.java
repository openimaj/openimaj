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
package org.openimaj.ml.linear.data;

import no.uib.cipr.matrix.Vector;

import org.junit.Test;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.ml.linear.learner.perceptron.PerceptronClass;
import org.openimaj.util.pair.IndependentPair;

/**
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class TestLinearPerceptronDataGenerator {

	/**
	 *
	 */
	@Test
	public void testDataGenerator() {
		final LinearPerceptronDataGenerator dg = new LinearPerceptronDataGenerator(100, 10, 0.3, 1);
		final Vector origin = dg.getOrigin();
		final Vector dir = dg.getNormDirection();
		// Point2d lineStart =
		start(origin, dir);
		// Point2d lineEnd =
		end(origin, dir);

		for (int i = 0; i < 100; i++) {
			final IndependentPair<double[], PerceptronClass> pointClass = dg.generate();

			final double[] pc = pointClass.firstObject();
			final Point2dImpl point = new Point2dImpl((float) pc[0], (float) pc[1]);
			final PerceptronClass cls = pointClass.getSecondObject();
			System.out.println(String.format("%s: %s", point, cls));
		}
	}

	private Point2d end(Vector origin, Vector dir) {
		final Vector ret = origin.copy().add(1000, dir);
		return new Point2dImpl((float) ret.get(0), (float) ret.get(1));
	}

	private Point2d start(Vector origin, Vector dir) {
		final Vector ret = origin.copy().add(-1000, dir);
		return new Point2dImpl((float) ret.get(0), (float) ret.get(1));
	}

}
