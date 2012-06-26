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
package org.openimaj.image.processing.morphology;

import org.openimaj.image.FImage;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.processor.ImageProcessor;
import org.openimaj.image.processor.connectedcomponent.ConnectedComponentProcessor;

/**
 * Morphological sequential thinning of connected components and (assumed binary) FImages.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class SequentialThin implements ConnectedComponentProcessor, ImageProcessor<FImage> {
	protected HitAndMiss hitAndMiss;
	protected int niter = -1;
	
	/**
	 * Construct the sequential thin operator with the given structuring elements
	 * @param se the structuring elements
	 */
	public SequentialThin(StructuringElement... se) {
		this.hitAndMiss = new HitAndMiss(se);
	}
	
	/**
	 * Construct the sequential thin operator with the given structuring elements
	 * and number of iterations
	 * @param niter number of iterations to apply
	 * @param se the structuring elements
	 */
	public SequentialThin(int niter, StructuringElement... se) {
		this.hitAndMiss = new HitAndMiss(se);
		this.niter = niter;
	}
	
	@Override
	public void process(ConnectedComponent cc) {		
		for (int i=niter; i!=0; i--) {
			hitAndMiss.process(cc);
			
			if (hitAndMiss.outputPixels.size() == 0)
				break;
			
			cc.getPixels().removeAll(hitAndMiss.outputPixels);
		}
	}

	/* (non-Javadoc)
	 * @see org.openimaj.image.processor.ImageProcessor#processImage(org.openimaj.image.Image)
	 */
	@Override
	public void processImage(FImage image) {
		for (int i=niter; i!=0; i--) {
			FImage newImage = image.process(hitAndMiss, true);
			
			int count = 0;
			for (int y=0; y<newImage.height; y++) {
				for (int x=0; x<newImage.width; x++) {
					if (newImage.pixels[y][x] == 1) {
						count++;
						image.pixels[y][x] = 0;
					}
				}
			}
			
			if (count == 0)
				break;
		}
	}
}
