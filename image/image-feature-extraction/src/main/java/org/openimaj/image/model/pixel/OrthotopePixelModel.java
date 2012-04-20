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
package org.openimaj.image.model.pixel;

import org.openimaj.image.MBFImage;

/**
 * Model of an orthotope/hyperrectangle/box in space. Everything inside classifies 
 * as positive, everything outside as negative.
 * 
 * @author Jonathon Hare
 */
public class OrthotopePixelModel extends MBFPixelClassificationModel {
	private static final long serialVersionUID = 1L;
	protected float [] min;
	protected float [] max;
	
	/**
	 * Construct with the given number of dimensions. This 
	 * should be equal to the number of bands in the {@link MBFImage}s
	 * you wish to classify.
	 * @param ndims number of dimensions
	 */
	public OrthotopePixelModel(int ndims) {
		super(ndims);
		
		min = new float[ndims];
		max = new float[ndims];
	}

	/**
	 * Construct with the given number box. The number of dimensions for
	 * each coordinate should be equal to the number of bands in the {@link MBFImage}s
	 * you wish to classify.
	 * 
	 * @param minCoords coordinates of the corner of the box with the smallest coordinates.
	 * @param maxCoords coordinates of the corner of the box with the largest coordinates.
	 */
	public OrthotopePixelModel(float [] minCoords, float [] maxCoords) {
		super(minCoords.length);
		
		if (minCoords.length != maxCoords.length)
			throw new IllegalArgumentException("minimum and maximum coordinates must have the same number of dimensions.");
		
		min = minCoords;
		max = maxCoords;
	}
	
	@Override
	protected float classifyPixel(Float[] pix) {
		
		for (int i=0; i<ndims; i++) {
			if (pix[i] > max[i] || pix[i] < min[i])
				return 0;
		}
		
		return 1;
	}

	@Override
	public OrthotopePixelModel clone() {
		OrthotopePixelModel newModel = new OrthotopePixelModel(ndims);
		
		newModel.min = min.clone();
		newModel.max = max.clone();

		return newModel;
	}

	@Override
	public void learnModel(MBFImage... images) {
		//Iterate through all the pixels and learn the bounding box
		
		for (int i=0; i<ndims; i++) {
			min[i] = Float.MAX_VALUE;
			max[i] = Float.MIN_VALUE;
		}
		
		for (MBFImage image : images) {
			for (int y=0; y<image.getHeight(); y++) {
				for (int x=0; x<image.getWidth(); x++) {
					Float [] pixel = image.getPixel(x, y);
					
					for (int i=0; i<ndims; i++) {
						if (pixel[i] > max[i]) max[i] = pixel[i];
						if (pixel[i] < min[i]) min[i] = pixel[i];
					}
				}
			}
		}
	}
}
