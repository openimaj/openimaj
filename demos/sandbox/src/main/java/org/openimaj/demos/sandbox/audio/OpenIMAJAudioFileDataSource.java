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
package org.openimaj.demos.sandbox.audio;

import java.io.File;
import java.net.URL;

import org.openimaj.audio.AudioStream;
import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.samples.SampleBuffer;
import org.openimaj.video.xuggle.XuggleAudio;

import edu.cmu.sphinx.frontend.BaseDataProcessor;
import edu.cmu.sphinx.frontend.Data;
import edu.cmu.sphinx.frontend.DataEndSignal;
import edu.cmu.sphinx.frontend.DataProcessingException;
import edu.cmu.sphinx.frontend.DataStartSignal;
import edu.cmu.sphinx.frontend.DoubleData;

/**
 * 	A wrapper that allows OpenIMAJ audio objects to be used as input
 * 	to a Sphinx4 speech recogniser.
 * 
 * 	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 * 	@created 7 Jun 2012
 * 	
 */
public class OpenIMAJAudioFileDataSource extends BaseDataProcessor
{
	/** Total number of values read */
	private long totalValuesRead = -1;
	
	/** The audio stream being used */
	private AudioStream audioStream = null;
	
	/** Set to true when we reach the end of the file */
	private boolean atEOF = false;

	/**
	 * 	Default constructor
	 */
	public OpenIMAJAudioFileDataSource()
	{
	}
	
	/**
	 * 	Construct an OpenIMAJ audio wrapper for Sphinx
	 *	@param as The audio stream to wrap
	 */
	public OpenIMAJAudioFileDataSource( AudioStream as )
	{
		this.audioStream = as;
	}
	
	/**
	 *	Reads data from the OpenIMAJ stream and creates Data packets for
	 *	Sphinx. Creates a {@link DataStartSignal} and {@link DataEndSignal}
	 *	at the beginning and end of the stream.	
	 *
	 * 	@see edu.cmu.sphinx.frontend.util.AudioFileDataSource#getData()
	 */
	@Override
	public Data getData() throws DataProcessingException
	{
		if( atEOF ) return null;
		
		getTimer().start();
		Data output = null;

		double sampleRate = audioStream.getFormat().
				getSampleRateKHz() * 1000;

		// First time through?
		if( totalValuesRead == -1 )
		{
			// If it's the first time through, we need to generate 
			// a DataSignalStart packet
			output = new DataStartSignal( (int)sampleRate );
			totalValuesRead = 0;
		}
		else
		{
			// Get some values we'll be needing
	        long collectTime = System.currentTimeMillis();
	        long firstSample = totalValuesRead;
	
	        // Get the next sample chunk from the audio stream
	        SampleChunk sc = audioStream.nextSampleChunk();
	        
	        // If we're at the end of the stream....
	        if( sc == null )
	        {
	        	// Data End Signal (duration in milliseconds)
	        	output = new DataEndSignal( (long)(totalValuesRead/sampleRate*1000) );
	        	atEOF = true;
	        }
	        else
	        {
	        	// Get a sample buffer from the chunk
	        	SampleBuffer b = sc.getSampleBuffer();
	        	
	        	// Keep a total of how many samples we've processed
		        totalValuesRead += b.size();
				
		        // Create the data output packet
		        output = new DoubleData( b.asDoubleArray(), (int)sampleRate, 
		        		collectTime, firstSample );
	        }
		}
        
		getTimer().stop();
		return output;
	}
	
	/**
	 * 	Set the audio file to read. Will instantiate a {@link XuggleAudio}
	 * 	to stream the data from.
	 *	@param url The audio file location
	 */
	public void setAudioFile( URL url )
	{
		this.audioStream = new XuggleAudio( url );
	}
	
	/**
	 * 	Set the audio file to read. Will instantiate a {@link XuggleAudio}
	 * 	to stream the data from.
	 *	@param f The audio file location
	 */
	public void setAudioFile( File f )
	{
		this.audioStream = new XuggleAudio( f );
	}
	
	/**
	 * 	Set the audio stream from which to read data.
	 *	@param as The audio stream.
	 */
	public void setAudioStream( AudioStream as )
	{
		this.audioStream = as;
	}
}
