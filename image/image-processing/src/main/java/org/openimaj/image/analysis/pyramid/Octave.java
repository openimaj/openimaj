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
package org.openimaj.image.analysis.pyramid;

import java.util.Iterator;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.processor.SinglebandImageProcessor;
import org.openimaj.util.array.ArrayIterator;

/**
 * An octave is an interval in scale space, typically corresponding to a 
 * doubling of sigma. Octaves contain a stack of one or more images, 
 * with each image typically at a higher scale than the previous.
 * 
 * Octaves are Iterable for easy access to each of the images in turn.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <OPTIONS> Type of options object
 * @param <PYRAMID> Type of parent pyramid
 * @param <IMAGE> Type of underlying image
 */
public abstract class Octave<
		OPTIONS extends PyramidOptions<?, IMAGE>, 
		PYRAMID extends Pyramid<OPTIONS,?,IMAGE>, 
		IMAGE extends Image<?,IMAGE> & SinglebandImageProcessor.Processable<Float,FImage,IMAGE>> 
	implements 
		Iterable<IMAGE> 
{
	/** The options used for the pyramid construction */
	public OPTIONS options;
	
	/** The images that make up this Octave */
	public IMAGE [] images;
	
	/** The pyramid that contains this Octave */
	public PYRAMID parentPyramid;
	
	/** The size of the octave relative to the original image. */
	public float octaveSize;
	
	/**
	 * Construct a Gaussian octave with the provided parent Pyramid
	 * and octaveSize. The octaveSize parameter is the size of 
	 * the octave's images compared to the original image used
	 * to construct the pyramid. An octaveSize of 1 means the 
	 * same size as the original, 2 means half size, 4 means 
	 * quarter size, etc.
	 * 
	 * @param parent the pyramid that this octave belongs to
	 * @param octaveSize the size of the octave relative to
	 * 			the original image.
	 */
	public Octave(PYRAMID parent, float octaveSize) {
		parentPyramid = parent;
		if (parent != null) this.options = parent.options;
		this.octaveSize = octaveSize;
	}
	
	/**
	 * Populate the octave, starting from the provided image.
	 * @param image the image.
	 */
	public abstract void process(IMAGE image);
	
	/**
	 * Get the image that starts the next octave. 
	 * Usually this is the image that has twice the sigma 
	 * of the image used to initialise this octave.
	 * 
	 * @return image image to start next octave.
	 */
	public abstract IMAGE getNextOctaveImage();

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<IMAGE> iterator() {
		return new ArrayIterator<IMAGE>(images);
	}
}
