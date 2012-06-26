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
package org.openimaj.image.feature.local.detector.pyramid;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.analysis.pyramid.Octave;
import org.openimaj.image.processor.SinglebandImageProcessor;

/**
 * Abstract base class for objects capable of detecting interest points
 * within an octave.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <OCTAVE> Type of underlying {@link Octave}
 * @param <IMAGE> Type of underlying {@link Image}
 */
public abstract class AbstractOctaveInterestPointFinder<
		OCTAVE extends Octave<?,?,IMAGE>, 
		IMAGE extends Image<?,IMAGE> & SinglebandImageProcessor.Processable<Float,FImage,IMAGE>> 
	implements 
		OctaveInterestPointFinder<OCTAVE, IMAGE> 
{
	/**
	 * The current octave.  
	 */
	protected OCTAVE octave;
	
	/**
	 * The index of the scale currently being processed within the octave.
	 * This should be changed as the finder progresses through the scales.
	 */
	protected int currentScaleIndex;
	
	/**
	 * The listener object that gets informed when interest points are detected.
	 */
	protected OctaveInterestPointListener<OCTAVE, IMAGE> listener;
	
	/* (non-Javadoc)
	 * @see dogsiftdevel.pyramid.OctaveInterestPointFinder#getOctave()
	 */
	@Override
	public OCTAVE getOctave() {
		return octave;
	}

	/* (non-Javadoc)
	 * @see dogsiftdevel.pyramid.OctaveInterestPointFinder#getCurrentScaleIndex()
	 */
	@Override
	public int getCurrentScaleIndex() {
		return currentScaleIndex;
	}

	/* (non-Javadoc)
	 * @see dogsiftdevel.pyramid.OctaveInterestPointFinder#setInterestPointListener(dogsiftdevel.pyramid.OctaveInterestPointListener)
	 */
	@Override
	public void setOctaveInterestPointListener(OctaveInterestPointListener<OCTAVE, IMAGE> listener) {
		this.listener = listener;
	}

	/* (non-Javadoc)
	 * @see dogsiftdevel.pyramid.OctaveInterestPointFinder#getInterestPointListener()
	 */
	@Override
	public OctaveInterestPointListener<OCTAVE, IMAGE> getOctaveInterestPointListener() {
		return listener;
	}
}
