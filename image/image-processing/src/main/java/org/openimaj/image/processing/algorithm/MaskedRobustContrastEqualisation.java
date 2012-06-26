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
package org.openimaj.image.processing.algorithm;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.image.FImage;
import org.openimaj.image.mask.AbstractMaskedObject;
import org.openimaj.image.processor.ImageProcessor;

/**
 * An image processor that performs contrast equalisation
 * in a robust manner with a mask.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Reference(
		type = ReferenceType.Article,
		author = { "Tan, Xiaoyang", "Triggs, Bill" },
		title = "Enhanced local texture feature sets for face recognition under difficult lighting conditions",
		year = "2010",
		journal = "Trans. Img. Proc.",
		pages = { "1635", "", "1650" },
		url = "http://dx.doi.org/10.1109/TIP.2010.2042645",
		month = "June",
		number = "6",
		publisher = "IEEE Press",
		volume = "19"
	)
public class MaskedRobustContrastEqualisation extends AbstractMaskedObject<FImage> implements ImageProcessor<FImage> {
	double alpha = 0.1;
	double tau = 10;

	/**
	 * Construct with no mask set
	 */
	public MaskedRobustContrastEqualisation() {
		super();
	}

	/**
	 * Construct with a mask.
	 * @param mask the mask.
	 */
	public MaskedRobustContrastEqualisation(FImage mask) {
		super(mask);
	}
	
	@Override
	public void processImage(FImage image) {
		//1st pass
		image.divideInplace(firstPassDivisor(image, mask));
		
		//2nd pass
		image.divideInplace(secondPassDivisor(image, mask));
		
		//3rd pass
		for (int y=0; y<image.height; y++) {
			for (int x=0; x<image.width; x++) {
				if (mask.pixels[y][x] == 1) {
					image.pixels[y][x] = (float) (tau * Math.tanh(image.pixels[y][x] / tau));
				} else {
					image.pixels[y][x] = 0;
				}
			}
		}
	}
	
	float firstPassDivisor(FImage image, FImage mask) {
		double accum = 0;
		int count = 0;
		
		for (int y=0; y<image.height; y++) {
			for (int x=0; x<image.width; x++) {
				if (mask.pixels[y][x] == 1) {
					double ixy = image.pixels[y][x];
					
					accum += Math.pow(Math.abs(ixy), alpha);
					count++;
				}
			}
		}
		
		return (float) Math.pow(accum / count, 1.0 / alpha);
	}
	
	float secondPassDivisor(FImage image, FImage mask) {
		double accum = 0;
		int count = 0;
		
		for (int y=0; y<image.height; y++) {
			for (int x=0; x<image.width; x++) {
				if (mask.pixels[y][x] == 1) {
					double ixy = image.pixels[y][x];
					
					accum += Math.pow(Math.min(tau, Math.abs(ixy)), alpha);
					count++;
				}
			}
		}
		
		return (float) Math.pow(accum / count, 1.0 / alpha);
	}
}
