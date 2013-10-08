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
