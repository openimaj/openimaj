/**
 *
 */
package org.openimaj.audio.analysis.benchmarking;

import java.util.List;
import java.util.ListIterator;

import org.openimaj.audio.AudioStream;
import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.samples.SampleBuffer;
import org.openimaj.data.dataset.ListDataset;

/**
 *
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 14 May 2013
 *	@version $Author$, $Revision$, $Date$
 */
public class AudioDatasetHelper
{
	/**
	 * 	From a dataset which is a list of sample buffers, this method will return an
	 * 	{@link AudioStream} which will return each of the sample buffers in turn.
	 *
	 *	@param samples The samples
	 *	@return The audio stream
	 */
	static public AudioStream getAudioStream( final ListDataset<List<SampleBuffer>> samples )
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

		final long streamLength = length;
		return new AudioStream()
		{
			private int index = 0;
			private List<SampleBuffer> l = null;
			private int listIndex = 0;

			@Override
			public void reset()
			{
				this.listIndex = 0;
				this.l = null;
			}

			@Override
			public SampleChunk nextSampleChunk()
			{
				if( this.l == null || this.index > this.l.size() )
				{
					this.l = samples.get(this.listIndex);
					this.index = 0;
					this.listIndex++;
				}

				return this.l.get(this.index++).getSampleChunk();
			}

			@Override
			public long getLength()
			{
				return streamLength;
			}
		};
	}
}
