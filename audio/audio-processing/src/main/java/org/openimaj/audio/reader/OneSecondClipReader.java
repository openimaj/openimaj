package org.openimaj.audio.reader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.processor.FixedSizeSampleAudioProcessor;
import org.openimaj.audio.samples.SampleBuffer;
import org.openimaj.io.InputStreamObjectReader;
import org.openimaj.video.xuggle.XuggleAudio;

/**
 *	This class reads an input stream that contains audio data, and returns a list of
 *	{@link SampleBuffer}s that each contain 1 second of audio. The number of samples in
 *	each sample buffer may not be the same, as the length of the buffers is calculated
 *	from the format of the audio in the stream.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 7 May 2013
 *	@version $Author$, $Revision$, $Date$
 */
public class OneSecondClipReader implements InputStreamObjectReader<List<SampleBuffer>>
{
	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.io.ObjectReader#read(java.lang.Object)
	 */
	@Override
	public List<SampleBuffer> read( final InputStream stream ) throws IOException
	{
		// Open the stream.
		final XuggleAudio xa = new XuggleAudio( stream );

		// Setup a chunker that will get samples in one second chunks.
		final int nSamplesInOneSecond = (int)(xa.getFormat().getSampleRateKHz() * 1000);
		final FixedSizeSampleAudioProcessor f = new FixedSizeSampleAudioProcessor(
				xa, nSamplesInOneSecond );

		// Setup our output list
		final List<SampleBuffer> buffers = new ArrayList<SampleBuffer>();

		// Now read the audio until we're done
		SampleChunk sc = null;
		while( (sc = f.nextSampleChunk()) != null )
			buffers.add( sc.getSampleBuffer() );

		System.out.println( "Got "+buffers.size()+" one-second sample buffers.");

		return buffers;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.io.InputStreamObjectReader#canRead(java.io.InputStream, java.lang.String)
	 */
	@Override
	public boolean canRead( final InputStream stream, final String name )
	{
		return true;
	}
}
