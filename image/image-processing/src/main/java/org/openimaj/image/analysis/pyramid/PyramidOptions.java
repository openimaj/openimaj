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

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.processor.SinglebandImageProcessor;

/**
 * Basic options for constructing a pyramid
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <OCTAVE> type of underlying octave
 * @param <IMAGE> type of underlying image.
 */
public class PyramidOptions<
	OCTAVE extends Octave<?,?,IMAGE>, 
	IMAGE extends Image<?,IMAGE> & SinglebandImageProcessor.Processable<Float,FImage,IMAGE>> 
{
	/**
	 * Should the Pyramid hold onto its octaves?
	 */
	protected boolean keepOctaves = false;
	
	/**
	 * An OctaveProcessor to apply to each octave of the pyramid. 
	 */
	protected OctaveProcessor<OCTAVE,IMAGE> octaveProcessor;
	
	/**
	 * PyramidProcessor for processing the pyramid after construction.
	 * Setting a PyramidProcessor will cause the Pyramid to hold onto
	 * its entire set of octaves.
	 */
	protected PyramidProcessor<IMAGE> pyramidProcessor = null;
	
	/**
	 * Get an OctaveProcessor to apply to each octave of the pyramid or null
	 * if none is set.
	 * 
	 * @return the octaveProcessor or null
	 */
	public OctaveProcessor<OCTAVE,IMAGE> getOctaveProcessor() {
		return octaveProcessor;
	}

	/**
	 * Gets the currently set PyramidProcessor or null if none is set.
	 * 
	 * PyramidProcessors process the pyramid after construction.
	 * Setting a PyramidProcessor will cause the Pyramid to hold onto
	 * its entire set of octaves.
	 * 
	 * @return the pyramidProcessor or null
	 */
	public PyramidProcessor<IMAGE> getPyramidProcessor() {
		return pyramidProcessor;
	}

	/**
	 * Determine whether the Pyramid should retain its octaves. 
	 * Value is overridden if a PyramidProcessor is set.
	 * @return the keepOctaves
	 */
	public boolean isKeepOctaves() {
		return keepOctaves;
	}

	/**
	 * Set whether the Pyramid should retain its octaves. 
	 * Default value is false, but is overridden if a PyramidProcessor
	 * is set.
	 * 
	 * @param keepOctaves the keepOctaves to set
	 */
	public void setKeepOctaves(boolean keepOctaves) {
		this.keepOctaves = keepOctaves;
	}

	/**
	 * Get an OctaveProcessor to apply to each octave of the pyramid. 
	 * Setting the OctaveProcessor to null disables it.
	 * 
	 * @param octaveProcessor the octaveProcessor to set
	 */
	public void setOctaveProcessor(OctaveProcessor<OCTAVE,IMAGE> octaveProcessor) {
		this.octaveProcessor = octaveProcessor;
	}

	/**
	 * Sets the PyramidProcessor.
	 * 
	 * PyramidProcessors for process the pyramid after construction.
	 * Setting a PyramidProcessor will cause the Pyramid to hold onto
	 * its entire set of octaves.
	 * 
	 * Setting the PyramidProcessor to null disables it.
	 * 
	 * @param pyramidProcessor the pyramidProcessor to set
	 */
	public void setPyramidProcessor(PyramidProcessor<IMAGE> pyramidProcessor) {
		this.pyramidProcessor = pyramidProcessor;
	}
}
