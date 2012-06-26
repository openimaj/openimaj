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
package org.openimaj.image.pixel.statistics;

import static org.junit.Assert.*;

import org.apache.commons.math.random.MersenneTwister;
import org.junit.Test;
import org.openimaj.image.FImage;
import org.openimaj.image.pixel.sampling.FLineSampler;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * Tests for {@link FStatisticalPixelProfileModel}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class FPixelProfileModelTest {
	/**
	 * Test with some random images
	 */
	@Test
	public void test1() {
		MersenneTwister mt = new MersenneTwister(99);
		
		FStatisticalPixelProfileModel ppm = new FStatisticalPixelProfileModel(5, FLineSampler.INTERPOLATED_DERIVATIVE);
		FImage img = new FImage(200, 200);
		for (int i=0; i<100; i++) {
			float gl = (float) (0.5 + (mt.nextGaussian() / 10.0));
			int x = (int) (100.0 + mt.nextGaussian() / 2.0);

			img.fill(0);
			img.drawShapeFilled(new Rectangle(100,0,100,200), gl);
			
			Line2d line = new Line2d(x+2, 50, x-2, 50);

			ppm.updateModel(img, line);
		}
		
		int x = 102;
		img.fill(0);
		img.drawShapeFilled(new Rectangle(100,0,100,200), 0.5f);
		while (true) {
			Line2d line = new Line2d(x+4, 50, x-4, 50);
		
			int newx = (int) ppm.computeNewBest(img, line, 9).getX();
			
			if (newx == x)
				break;
				
			x = newx;
		}
		
		assertEquals(100, x);
	}
}
