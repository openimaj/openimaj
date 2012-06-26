/**
 * 
 */
package org.openimaj.demos.sandbox.audio;

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
	 * 	Set the audio stream from which to read data.
	 *	@param as The audio stream.
	 */
	public void setAudioStream( AudioStream as )
	{
		this.audioStream = as;
	}
}
