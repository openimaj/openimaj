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

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 *	Wraps the Java Sound APIs into the OpenIMAJ audio core.
 *
 *	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *  @created 8 Jun 2011
 *	@version $Author$, $Revision$, $Date$
 */
public class AudioPlayer implements Runnable
{
	/** The audio stream being played */
	private AudioStream stream = null;
	
	/** The java audio output stream line */
	private SourceDataLine mLine = null;
	
	/**
	 * 	Default constructor that takes an audio
	 * 	stream to play.
	 * 
	 *	@param a The audio stream to play
	 */
	public AudioPlayer( AudioStream a )
	{
		this.stream = a;
	}
	
	/**
	 *	@inheritDoc
	 * 	@see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
		try
		{
			// Open the sound system.
			openJavaSound();
			
			// Read samples until there are no more.
			SampleChunk samples = null;
			while( (samples = stream.nextSampleChunk()) != null )
			{
				playJavaSound( samples );
			}
			
			// Close the sound system
			closeJavaSound();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * 	Create a new audio player in a separate thread that will
	 * 	start playing the audio.
	 * 
	 *	@param as The audio stream to play.
	 *	@return The audio player created.
	 */
	public static AudioPlayer createAudioPlayer( AudioStream as )
	{
		AudioPlayer ap = new AudioPlayer( as );
		new Thread( ap ).start();
		return ap;
	}
	
	/**
	 * 	Open a line to the Java Sound APIs.
	 * 
	 *	@throws Exception if the Java sound system could not be initialised.
	 */
	private void openJavaSound() throws Exception
	{
		// Convert the OpenIMAJ audio format to a Java Sound audio format object
		javax.sound.sampled.AudioFormat audioFormat =
		new javax.sound.sampled.AudioFormat(
				(int)this.stream.getFormat().getSampleRateKHz()*1000,
				this.stream.getFormat().getNBits(),
				this.stream.getFormat().getNumChannels(),
				this.stream.getFormat().isSigned(),
				this.stream.getFormat().isBigEndian() );
		
		System.out.println( "Creating Java Sound Line with "+
				this.stream.getFormat() );
		
		// Create info to create an output data line
		DataLine.Info info = new DataLine.Info(	
				SourceDataLine.class, audioFormat );
		
		try
		{
			// Get the output line to write to using the given
			// sample format we just created.
			mLine = (SourceDataLine)AudioSystem.getLine( info );

			// If no exception has been thrown we open the line.
			mLine.open( audioFormat );

			// If we've opened the line, we start it running
			mLine.start();
		}
		catch( LineUnavailableException e )
		{
			throw new Exception( "Could not open Java Sound audio line for" +
					" the audio format "+stream.getFormat() );
		}
	}

	/**
	 * 	Play the given sample chunk to the Java sound line. The line should be
	 * 	set up to accept the samples that we're going to give it, as we did
	 * 	that in the {@link #openJavaSound()} method.
	 * 
	 *	@param chunk The chunk to play.
	 */
	private void playJavaSound( SampleChunk chunk )
	{
		byte[] rawBytes = chunk.getSamples();
		//System.out.println( debugByteArray( rawBytes ) );
		mLine.write( rawBytes, 0, rawBytes.length );
	}

	public String debugByteArray( byte[] b )
	{
		StringBuffer s = new StringBuffer();
		s.append("[");
		for( byte bb : b )
			s.append( bb+"," );
		s.append("]");
		return s.toString();
	}
	
	/**
	 * 	Close down the Java sound APIs.
	 */
	private void closeJavaSound()
	{
		if( mLine != null )
		{
			// Wait for the buffer to empty...
			mLine.drain();
			
			// ...then close
			mLine.close();
			mLine = null;
		}
	}

}
