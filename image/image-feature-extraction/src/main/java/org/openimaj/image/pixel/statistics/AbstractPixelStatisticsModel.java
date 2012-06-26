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

import java.io.Serializable;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;


/**
 * Abstract base class for models based on pixel statistics of an image.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public abstract class AbstractPixelStatisticsModel implements Cloneable, Serializable {
	private static final long serialVersionUID = 1L;
	
	protected int ndims;
	
	/**
	 * Construct the model with the given number of dimensions
	 * @param ndims number of dimensions
	 */
	public AbstractPixelStatisticsModel(int ndims) {
		this.ndims = ndims;
	}
	
	/**
	 * Estimate the model parameters from the given image(s).
	 * @param images list of images
	 */
	public abstract void estimateModel(MBFImage... images);
	
	/**
	 * Estimate the model parameters from the given image(s). Internally
	 * this method constructs 1-band MBFImages.
	 * 
	 * @see #estimateModel(MBFImage[])
	 * @param images images list of images
	 */
	public void estimateModel(FImage... images) {
		MBFImage[] imgs = new MBFImage[images.length];
		for (int i=0; i<images.length; i++)
			imgs[i] = new MBFImage(images[i]);
		
		estimateModel(imgs);
	}
}
