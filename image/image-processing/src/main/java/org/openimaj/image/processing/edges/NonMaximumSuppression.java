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
package org.openimaj.image.processing.edges;

import org.openimaj.image.FImage;
import org.openimaj.image.combiner.ImageCombiner;

/**
 * Non-maximum suppression using magnitude and orientation images.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class NonMaximumSuppression implements ImageCombiner<FImage, FImage, FImage> {
	/**
	 * Perform non-maximum suppression.
	 * 
	 * @param mag Gradient magnitudes
	 * @param ori Gradient orientations
	 * @return non-maximum suppressed magnitude image.
	 */
	public static FImage computeSuppressed(FImage mag, FImage ori) {
		int height = mag.getHeight(), width = mag.getWidth();
	
		FImage suppressed = new FImage(width, height);

		float p8 = (float) (Math.PI / 8.0);
		
		//Compute max suppresion	
		for (int y=1; y<height-1; y++) {
			for (int x=1; x<width-1; x++) {
				if (mag.pixels[y][x] > 0) {
					float t = (float) (ori.pixels[y][x] - Math.PI/2);   //angle of edge in -pi to pi
					
					if ((t>=-p8 && t<=p8) || (t<=-7*p8 || t>=7*p8)) { //zero degrees or 180 degrees
						if (mag.pixels[y][x] > mag.pixels[y+1][x] && mag.pixels[y][x] >= mag.pixels[y-1][x]) {
							suppressed.pixels[y][x] = mag.pixels[y][x];
						}
					} else if ((t>=3*p8 && t<=5*p8) || (t>=-5*p8 && t<=-3*p8)) { //+/-90 degrees
						if (mag.pixels[y][x] >= mag.pixels[y][x+1] && mag.pixels[y][x] > mag.pixels[y][x-1]) {
							suppressed.pixels[y][x] = mag.pixels[y][x];
						}
					} else if ((t>=p8 && t<=3*p8) || (t>=-7*p8 && t<=-5*p8)) { //+45 degrees or -135 degrees
						if (mag.pixels[y][x] > mag.pixels[y+1][x-1] && mag.pixels[y][x] >= mag.pixels[y-1][x+1]) {
							suppressed.pixels[y][x] = mag.pixels[y][x];
						}
					} else {
						if (mag.pixels[y][x] > mag.pixels[y-1][x-1] && mag.pixels[y][x] >= mag.pixels[y+1][x+1]) {
							suppressed.pixels[y][x] = mag.pixels[y][x];
						}
					}
				}
			}
		}
		
		return suppressed;
	}

	/**
	 * Perform non-maximum suppression.
	 * 
	 * @param mag Gradient magnitudes
	 * @param ori Gradient orientations
	 * @return non-maximum suppressed magnitude image.
	 * 
	 * @see org.openimaj.image.combiner.ImageCombiner#combine(org.openimaj.image.Image, org.openimaj.image.Image)
	 */
	@Override
	public FImage combine(FImage mag, FImage ori) {
		return computeSuppressed(mag, ori);
	}
}
