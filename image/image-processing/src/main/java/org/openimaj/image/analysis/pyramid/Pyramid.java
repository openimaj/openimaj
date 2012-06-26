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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.processor.SinglebandImageProcessor;

/**
 * An image pyramid consisting of a stack of octaves.
 * 
 * Octaves are processed by an OctaveProcessor as they are created
 * if the processor is set in the options object.
 * 
 * The pyramid will only hold onto its octaves if either the 
 * keepOctaves option is set to true, or if a PyramidProcessor is
 * set in the options. The PyramidProcessor will called after all
 * the octaves are created.
 * 
 * Pyramids are Iterable for easy access to the octaves; however this
 * will only work if the pyramid has already been populated with the
 * octaves retained.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <OPTIONS> Type of options object
 * @param <OCTAVE> Type of underlying octave
 * @param <IMAGE> Type of underlying image
 */
public abstract class Pyramid<
		OPTIONS extends PyramidOptions<OCTAVE,IMAGE>, 
		OCTAVE extends Octave<OPTIONS,?,IMAGE>, 
		IMAGE extends Image<?,IMAGE> & SinglebandImageProcessor.Processable<Float,FImage,IMAGE>> 
	implements 
		ImageAnalyser<IMAGE>, Iterable<OCTAVE> 
{
	/**
	 * Options for the pyramid
	 */
	protected OPTIONS options;
	
	/**
	 * A list of all the octaves should you want to keep them.
	 * @see PyramidOptions.keepOctaves 
	 */
	protected List<OCTAVE> octaves;
	
	/**
	 * Construct a Pyramid with the given options.
	 * @param options the options
	 */
	public Pyramid(OPTIONS options) {
		this.options = options;
		
		if (options.keepOctaves || options.pyramidProcessor!=null)
			octaves = new ArrayList<OCTAVE>();
	}
	
	/**
	 * Process the image and construct a pyramid applying any specified
	 * OctaveProcessor and/or PyramidProcessor along the way. If a 
	 * PyramidProcessor is specified or the options have keepOctaves set 
	 * to true, then the octaves of the pyramid will be retained.
	 * 
	 * @param img image to build pyramid from.
	 */
	public abstract void process(IMAGE img);
	
	/* (non-Javadoc)
	 * @see org.openimaj.image.analyser.ImageAnalyser#analyseImage(org.openimaj.image.Image)
	 */
	@Override
	public void analyseImage(IMAGE image) {
		process(image);
	}

	/**
	 * Get the options used to initialise this pyramid
	 * @return the options
	 */
	public OPTIONS getOptions() {
		return options;
	}
	
	/**
	 * Set the options used to initialise this pyramid
	 * @param options the options
	 */
	public void setOptions(OPTIONS options) {
		this.options = options;
	}

	/**
	 * Get the octaves of this pyramid if they have
	 * been retained by the processing.
	 * 
	 * @return the octaves
	 */
	public List<OCTAVE> getOctaves() {
		return octaves;
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<OCTAVE> iterator() {
		if (octaves == null) return null;
		return octaves.iterator();
	}
}
