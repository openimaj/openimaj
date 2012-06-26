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
package org.openimaj.image.processing.convolution;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openimaj.image.FImage;

/**
 * Test {@link FConvolution}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class FConvolutionTest {
	/**
	 * Test consistency between modes.
	 */
	@Test
	public void testConsistency() {
		FImage kernel = new FImage(3,3);
		kernel.addInplace(1f);
		
		FImage kernelRow = new FImage(3,1);
		FImage kernelCol = new FImage(1,3);
		
		kernelRow.addInplace(3f);
		kernelCol.addInplace(3f);
		
		FImage im = new FImage(10,10);
		im.addInplace(1f);
		
		FConvolution conAutoSep = new FConvolution(kernel);
		FConvolution conBrute = new FConvolution(kernel);
		FConvolution conAutoRow = new FConvolution(kernelRow);
		FConvolution conAutoCol = new FConvolution(kernelCol);
		
		conBrute.setBruteForce(true);
		
		assertTrue(im.process(conAutoSep).equalsThresh(im.multiply(9f), 0.001f));
		assertTrue(im.process(conAutoRow).equalsThresh(im.multiply(9f), 0.001f));
		assertTrue(im.process(conAutoCol).equalsThresh(im.multiply(9f), 0.001f));
		assertTrue(im.process(conBrute).extractROI(1, 1, im.width-3, im.height-3).equalsThresh(im.multiply(9f).extractROI(1, 1, im.width-3, im.height-3), 0.001f));
	}
}
