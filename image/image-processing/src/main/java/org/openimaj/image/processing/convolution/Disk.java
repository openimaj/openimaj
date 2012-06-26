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

import org.openimaj.image.FImage;
import org.openimaj.math.util.FloatArrayStatsUtils;

/**
 * Disk - a circular averaging filter.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class Disk extends FConvolution {
	
	/**
	 * Default constructor. Makes a disk averaging filter
	 * with the given radius.
	 * @param radius the radius.
	 */
	public Disk(int radius) {
		super(createKernelImage(radius));
	}

	/**
	 * Makes a disk averaging filter with the given radius.
	 * @param radius
	 * @return the filter image
	 */
	public static FImage createKernelImage(int radius) {
		int sze = 2*radius+1;
		FImage f = new FImage(sze, sze);
		int hsz = (sze-1)/2;
		
		for (int y=-hsz, j=0; y<hsz; y++, j++) {
			for (int x=-hsz, i=0; x<hsz; x++, i++) {
				double rad = Math.sqrt(x*x + y*y);
				f.pixels[j][i] = rad < radius ? 1 : 0; 				
			}
		}
		float sum = FloatArrayStatsUtils.sum(f.pixels);
		return f.divideInplace(sum);		
	}
}
