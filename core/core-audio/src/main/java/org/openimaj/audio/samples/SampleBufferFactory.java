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
package org.openimaj.audio.samples;

import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.SampleChunk;

/**
 * Factory for creating {@link SampleBuffer}s from {@link AudioFormat}s.
 * 
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 23rd November 2011
 */
public abstract class SampleBufferFactory {
	/**
	 * Create a {@link SampleBuffer}.
	 * 
	 * @param af
	 * @param size
	 * @return new {@link SampleBuffer}.
	 */
	public static SampleBuffer createSampleBuffer(AudioFormat af, int size) {
		switch (af.getNBits()) {
		case 8:
			return new SampleBuffer8Bit(af, size);
		case 16:
			return new SampleBuffer16Bit(af, size);
		default:
			return null;
		}
	}

	/**
	 * Create a {@link SampleBuffer}.
	 * 
	 * @param s
	 * @param af
	 * @return new {@link SampleBuffer}.
	 */
	public static SampleBuffer createSampleBuffer(SampleChunk s, AudioFormat af) {
		switch (af.getNBits()) {
		case 8:
			return new SampleBuffer8Bit(s, af);
		case 16:
			return new SampleBuffer16Bit(s, af);
		default:
			return null;
		}
	}
}
