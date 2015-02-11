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
/**
 *
 */
package org.openimaj.audio.analysis;

import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.filters.VolumeAdjustProcessor;
import org.openimaj.audio.processor.AudioProcessor;
import org.openimaj.audio.samples.SampleBuffer;

/**
 * Calculates the scalar necessary to achieve peak value of the loudest part of
 * the given input signal. This scalar can be retrieved using the
 * {@link #getVolumeScalar()} method which will return the scalar as it has
 * currently been calculated for the stream which has passed. This will only
 * return the correct value for the whole audio stream once the whole audio
 * stream has passed through the processor. This processor will not perform
 * attenuation - that is, the volume scalar will always be greater than 1 (and
 * positive).
 * <p>
 * The value of the peak volume scalar is compatible with the
 * {@link VolumeAdjustProcessor} which can adjust the volume to the maximum peak
 * value.
 *
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 10 Dec 2012
 * @version $Author$, $Revision$, $Date$
 */
public class PeakNormalisationCalculator extends AudioProcessor
{
	/** The peak scalar as it's been calcultated so far in the stream */
	private float scalar = Float.MAX_VALUE;

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.audio.processor.AudioProcessor#process(org.openimaj.audio.SampleChunk)
	 */
	@Override
	public SampleChunk process(final SampleChunk sample) throws Exception
	{
		// This allows us to retrieve samples that are scaled to 0..1 float
		// values
		final SampleBuffer sb = sample.getSampleBuffer();

		for (final float s : sb)
			if (Math.abs(s) * this.scalar > 1)
				this.scalar = 1 / Math.abs(s);

		return sample;
	}

	/**
	 * Returns the calculated peak scalar as it currently stands in the stream.
	 * If no audio has passed through the processor, this will return
	 * {@link Float#MAX_VALUE} otherwise the value will always be positive and
	 * greater than 1.
	 *
	 * @return The calculated peak scalar.
	 */
	public float getVolumeScalar()
	{
		return this.scalar;
	}
}
