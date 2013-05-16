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
