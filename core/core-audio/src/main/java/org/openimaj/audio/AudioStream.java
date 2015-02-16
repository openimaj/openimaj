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
package org.openimaj.audio;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Represents an audio stream that can be read chunk-by-chunk.
 *
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 8 Jun 2011
 *
 */
public abstract class AudioStream extends Audio implements Iterable<SampleChunk>
{
	/**
	 * Retrieve the next SampleChunk from the audio stream.
	 *
	 * @return The next sample chunk in the audio stream.
	 */
	public abstract SampleChunk nextSampleChunk();

	/**
	 * Reset the audio stream.
	 */
	public abstract void reset();

	/**
	 * Returns the length of the audio stream in milliseconds. If the length is
	 * unknown (for a live stream for example), then this method should return
	 * -1.
	 *
	 * @return The length in milliseconds, or -1
	 */
	public abstract long getLength();

	/**
	 * Seeks the audio to the given timestamp. The timestamp of the audio should
	 * be checked after calling seek() as the seek method may not succeed if the
	 * stream does not support seeking.
	 *
	 * @param timestamp
	 *            The timestamp to seek to
	 */
	public void seek(final long timestamp)
	{
		// Seek supported? Then override this method.
	}

	@Override
	public Iterator<SampleChunk> iterator() {
		return new Iterator<SampleChunk>() {
			private SampleChunk next;

			@Override
			public boolean hasNext() {
				if (next != null)
					return true;
				next = nextSampleChunk();
				return next != null;
			}

			@Override
			public SampleChunk next() {
				if (next == null)
					next = nextSampleChunk();

				final SampleChunk ret = next;
				next = null;

				if (ret == null)
					throw new NoSuchElementException();

				return ret;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Remove not supported");
			}
		};
	}
}
