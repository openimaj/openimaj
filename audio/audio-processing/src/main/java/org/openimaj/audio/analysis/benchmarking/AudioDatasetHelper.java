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
package org.openimaj.audio.analysis.benchmarking;

import java.util.List;
import java.util.ListIterator;

import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.AudioStream;
import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.samples.SampleBuffer;
import org.openimaj.data.dataset.ListDataset;

/**
 *	A class for helping to deal with datasets of audio information.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 14 May 2013
 *	@version $Author$, $Revision$, $Date$
 */
public class AudioDatasetHelper
{
	/**
	 * 	From a dataset that contains sample buffers, this method will return an
	 * 	{@link AudioStream} which will return each of the sample buffers in turn.
	 * 	The returned stream will have an undefined length (-1). If you need to know
	 * 	the length of the stream, use {@link #calculateStreamLength(ListDataset)}.
	 *
	 *	@param samples The samples
	 *	@return The audio stream
	 */
	static public AudioStream getAudioStream( final ListDataset<List<SampleBuffer>> samples )
	{
		final long streamLength = -1;
		return new AudioStream()
		{
			private int index = 0;
			private List<SampleBuffer> l = null;
			private int listIndex = 0;
			private long timecodeOffset = 0;
			private long currentTimecode = 0;

			@Override
			public void reset()
			{
				this.listIndex = 0;
				this.l = null;
				this.index = 0;
				this.timecodeOffset = 0;
				this.currentTimecode = 0;
			}

			@Override
			public SampleChunk nextSampleChunk()
			{
				if( this.listIndex >= samples.size() )
					return null;

				if( this.l == null || this.index >= this.l.size() )
				{
					this.l = samples.get(this.listIndex);
					this.index = 0;
					this.listIndex++;
					this.timecodeOffset += this.currentTimecode;
				}

				// Get the current sample chunk
				final SampleChunk sc = this.l.get(this.index).getSampleChunk();

				// Work out the timecode at the end of the sample chunk
				this.currentTimecode = (long)(sc.getStartTimecode().getTimecodeInMilliseconds() +
						sc.getNumberOfSamples() / sc.getFormat().getSampleRateKHz());

				// Add the offset into the current timecode.
				sc.getStartTimecode().setTimecodeInMilliseconds(
					sc.getStartTimecode().getTimecodeInMilliseconds() + this.timecodeOffset );

				this.index++;

				return sc;
			}

			@Override
			public long getLength()
			{
				return streamLength;
			}

			@Override
			public AudioFormat getFormat()
			{
				final SampleChunk sc = this.nextSampleChunk();
				this.reset();
				return sc.getFormat();
			}
		};
	}

	/**
	 * 	Calculate the length of the stream of samples that will come from the dataset.
	 *
	 *	@param samples The sample list
	 *	@return The length of the stream in milliseconds
	 */
	public static long calculateStreamLength( final ListDataset<List<SampleBuffer>> samples )
	{
		final ListIterator<List<SampleBuffer>> i = samples.listIterator();
		long length = 0;
		while( i.hasNext() )
		{
			final List<SampleBuffer> l = i.next();
			for( final SampleBuffer sb : l )
			{
				length += sb.size() / sb.getFormat().getNumChannels() * sb.getFormat().getSampleRateKHz();
			}
		}

		return length;
	}
}
