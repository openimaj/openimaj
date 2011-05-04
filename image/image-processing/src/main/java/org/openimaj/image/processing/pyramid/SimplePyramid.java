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
package org.openimaj.image.processing.pyramid;

import java.lang.reflect.Array;
import java.util.Iterator;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.image.processor.ImageProcessor;
import org.openimaj.image.processor.SinglebandImageProcessor;
import org.openimaj.util.array.ArrayIterator;

/**
 * A simple image pyramid built as a stack of images. For convenience,
 * when applied to an image, the last level of the pyramid will be 
 * assigned to the input image.
 * 
 * SimplePyramids are @link{Iterable}, so you can iterate over the levels.
 * 
 * @author Sina Samangooei <ss@ecs.soton.ac.uk>
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 * 
 * @param <IMAGE> the underlying image type 
 */
public class SimplePyramid<
		IMAGE extends Image<?,IMAGE> & SinglebandImageProcessor.Processable<Float,FImage,IMAGE>> 
	implements 
		ImageProcessor<IMAGE>, 
		Iterable<IMAGE> 
{
	/**
	 * The images forming the pyramid
	 */
	public IMAGE[] pyramid;
	
	/**
	 * The factor by which each level changes in size.
	 * Numbers >1 imply shrinking between levels. 
	 */
	float power;
	
	/**
	 * The number 
	 */
	int nlevels;
	
	/**
	 * Construct a pyramid with the given scale factor. The number
	 * of levels is such that the lowest level of the pyramid is a
	 * minimum of 8 pixels on its shortest side.
	 * 
	 * @param power scale factor between levels
	 */
	public SimplePyramid(float power) {
		this.power = power;
		this.nlevels = -1;
	}
	
	/**
	 * Construct a pyramid with the given scale factor and
	 * number of levels. If the number of levels is zero or
	 * less, then the actual number of levels will be calculated
	 * dynamically so the shortest side of the bottom level has
	 * at least 8 pixels.
	 * 
	 * @param power scale factor between levels
	 * @param nlevels number of levels
	 */
	public SimplePyramid(float power, int nlevels) {
		this.power = power;
		this.nlevels = nlevels;
	}
	
	/**
	 * compute the number of levels such that the minimum size
	 * is at least 8. 
	 * @param size size
	 * @return number of levels
	 */
	protected int computeLevels(int size) {
		int levels = 1;
		while (true) {
			size /= power;
			
			if (size < 8) break;
			
			levels++;
		}
		
		return levels;
	}
	
	/* (non-Javadoc)
	 * @see org.openimaj.image.processor.ImageProcessor#processImage(I, Image[])
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void processImage(IMAGE image, Image<?, ?>... otherimages) {
		if (nlevels <= 0) nlevels = computeLevels(Math.min(image.getWidth(), image.getHeight()));
		
		this.pyramid = (IMAGE[]) Array.newInstance(image.getClass(), nlevels);
		IMAGE original = image;
		
		for (int i=0; i<nlevels; i++) {
			pyramid[i]=image;

			int m = (int) Math.floor(image.getHeight() / power);
			int n = (int) Math.floor(image.getWidth() / power);

			image = image.process(new ResizeProcessor(n, m));
		}
		
		original.internalAssign(pyramid[nlevels-1]);
	}

	@Override
	public Iterator<IMAGE> iterator() {
		return new ArrayIterator<IMAGE>(pyramid);
	}
}
