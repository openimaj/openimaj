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
import org.openimaj.image.analysis.pyramid.OctaveProcessor;
import org.openimaj.image.processor.SinglebandImageProcessor;

/**
 * Interface for objects that can detect interest points within an
 * octave.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <OCTAVE> Type of underlying {@link Octave}
 * @param <IMAGE> Type of underlying {@link Image}
 */
public interface OctaveInterestPointFinder<
		OCTAVE extends Octave<?,?,IMAGE>, 
		IMAGE extends Image<?,IMAGE> & SinglebandImageProcessor.Processable<Float,FImage,IMAGE>> 
	extends 
		OctaveProcessor<OCTAVE,IMAGE> 
{
	/**
	 * Get the octave from which we are operating
	 * 
	 * @return the octave
	 */
	public OCTAVE getOctave();
	
	/**
	 * Get the current scale index within the octave. Index 0 is the
	 * first level of the octave.
	 * 
	 * @return the current scale index.
	 */
	public int getCurrentScaleIndex();
	
	/**
	 * Set a listener object that will listen to events triggered when interest
	 * points are detected.
	 * 
	 * @param listener the listener 
	 */
	public void setOctaveInterestPointListener(OctaveInterestPointListener<OCTAVE, IMAGE> listener);
	
	/**
	 * Get the current listener object.
	 * 
	 * @return the listener, or null if none set.
	 */
	public OctaveInterestPointListener<OCTAVE,IMAGE> getOctaveInterestPointListener();
}
