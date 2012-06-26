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
package org.openimaj.math.geometry.shape.algorithm;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.PointList;
import org.openimaj.math.geometry.shape.Triangle;

import cern.jet.random.Uniform;
import cern.jet.random.engine.MersenneTwister;

/**
 * Tests for {@link ProcrustesAnalysis}
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class GeneralisedProcrustesAnalysisTest {

	/**
	 * Test alignment by generating random transforms
	 * and then recovering them
	 */
	@Test
	public void testAlignment() {
		Uniform rnd = new Uniform(new MersenneTwister(1));
		List<PointList> shapes = new ArrayList<PointList>();
		
		for (int i=0; i<10; i++) {
			shapes.add(randomTriangle(rnd));
		}

		GeneralisedProcrustesAnalysis.alignPoints(shapes, 10f, 10000);
	}

	PointList randomTriangle(Uniform rnd) {
		return new Triangle(
			new Point2dImpl(rnd.nextFloatFromTo(0, 200), rnd.nextFloatFromTo(0, 200)), 
			new Point2dImpl(rnd.nextFloatFromTo(0, 200), rnd.nextFloatFromTo(0, 200)),
			new Point2dImpl(rnd.nextFloatFromTo(0, 200), rnd.nextFloatFromTo(0, 200))
		).asPolygon();
	}
}
