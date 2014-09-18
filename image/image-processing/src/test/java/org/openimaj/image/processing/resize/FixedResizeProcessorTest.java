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
package org.openimaj.image.processing.resize;

import java.io.IOException;

import org.junit.Test;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class FixedResizeProcessorTest {
	/**
	 * @throws IOException
	 */
	@Test
	public void testFixedResize() throws IOException {
		final FImage image = ImageUtilities.readF(ResizeProcessorTest.class
				.getResourceAsStream("/org/openimaj/image/data/sinaface.jpg"));
		long start, end;
		start = System.currentTimeMillis();
		final FixedResizeProcessor frp = new FixedResizeProcessor(image, 500, 250);
		for (int i = 0; i < 10000; i++) {
			image.process(frp);
		}
		end = System.currentTimeMillis();
		System.out.println("Time taken (fixed): " + (end - start));

		final ResizeProcessor rp = new ResizeProcessor(500, 250);
		start = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			image.process(rp);
		}
		end = System.currentTimeMillis();
		System.out.println("Time taken (normal): " + (end - start));
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		new FixedResizeProcessorTest().testFixedResize();
	}

}
