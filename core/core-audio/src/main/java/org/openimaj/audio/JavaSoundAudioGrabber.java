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

import java.io.ByteArrayOutputStream;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

/**
 * Audio grabber that uses the Java Sound API as a sound source.
 * 
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * 
 * @created 28 Oct 2011
 */
public class JavaSoundAudioGrabber extends AudioGrabber 
{
	/** The current sample chunk */
	private SampleChunk currentSample = new SampleChunk( getFormat() );

	/** The Java Sound data line being used to write to */
	private TargetDataLine mLine = null;

	/** Whether the stream is grabbing or not */
	private boolean stopped = true;
	
	/** The minimum buffer size required */
	private int maxBufferSize = -1;

	/**
	 * Default constructor
	 */
	public JavaSoundAudioGrabber()
	{
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.audio.AudioGrabber#run()
	 */
	@Override
	public void run()
	{
		try
		{
			openJavaSound();

			// Setup a byte array into which to store the bytes we read from
			// the Java Sound mixer. It's smaller than the mixer's buffer so
			// that we have time to process stuff before the mixer needs to
			// refill its buffer.
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			int numBytesRead = 0;
			final byte[] data = new byte[calculateBufferSize()];

			// Begin audio capture.
			mLine.start();

			// Keep going until we're told to top
			stopped = false;
			while( !stopped )
			{
				// Read the next chunk of data from the TargetDataLine.
				numBytesRead = mLine.read( data, 0, data.length );

				// Save this chunk of data.
				out.write( data, 0, numBytesRead );

				// synced on current sample so that the nextSampleChunk() method
				// can wait until the buffer is full
				synchronized( currentSample )
				{
					// Set the samples in our sample chunk
					currentSample.setSamples( data.clone() );
					currentSample.notify();
				}
					
				// Let the listeners know
				fireAudioAvailable();
			}

			closeJavaSound();
			System.out.println("Stopping java sound");
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * 	From http://www.javadocexamples.com/java_source/ccs/chaos/NoiseGrabber.java.html
	 * 	Not sure if this will work for non 44.1KHz samples?
	 *  @return
	 */
	private int calculateBufferSize()
	{
        int nmax = (maxBufferSize == -1 ? mLine.getBufferSize() / 4 : maxBufferSize);
        final int[] FAC44100 = { 7, 7, 5, 5, 3, 3, 2, 2 };
        int nwad = 1;
        for( int i = 0; i < 8; i++ ) 
        	if( nwad*FAC44100[i] <= nmax ) 
        		nwad *= FAC44100[i];
        
        return nwad << (getFormat().getNBits()/8);
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.audio.AudioGrabber#stop()
	 */
	@Override
	public void stop()
	{
		this.stopped = true;
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.audio.AudioGrabber#isStopped()
	 */
	@Override
	public boolean isStopped()
	{
		return this.stopped;
	}

	/**
	 * 	Fire the event and audio is now available
	 */
	@Override
	protected void fireAudioAvailable()
	{
		for( AudioGrabberListener l : listeners )
			l.samplesAvailable( currentSample );
	}
	
	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.audio.Audio#setFormat(org.openimaj.audio.AudioFormat)
	 */
	@Override
	public void setFormat( AudioFormat format )
	{
		currentSample.setFormat( format );
	    super.setFormat( format );
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.openimaj.audio.AudioStream#nextSampleChunk()
	 */
	@Override
	public SampleChunk nextSampleChunk()
	{
		synchronized( currentSample )
        {
			if(this.isStopped()) return null;
			try
            {
	            currentSample.wait();
            }
            catch( InterruptedException e )
            {
	            e.printStackTrace();
            }
            
			return currentSample;	        
        }
	}

	/**
	 * 	Set the maximum size buffer to be returned. 
	 *  @param maxBufferSize
	 */
	public void setMaxBufferSize( int maxBufferSize )
	{
		this.maxBufferSize = maxBufferSize;
	}
	
	/**
	 * Open a line to the Java Sound APIs.
	 * @throws Exception
	 *     if the Java sound system could not be initialised.
	 */
	private void openJavaSound() throws Exception
	{
		// Convert the OpenIMAJ audio format to a Java Sound audio format object
		javax.sound.sampled.AudioFormat audioFormat = new javax.sound.sampled.AudioFormat(
		        (int)this.getFormat().getSampleRateKHz() * 1000, this
		                .getFormat().getNBits(), this.getFormat()
		                .getNumChannels(), this.getFormat().isSigned(), this
		                .getFormat().isBigEndian() );

		System.out.println( "Creating Java Sound Line with " + this.getFormat() );

		// Create info to create an output data line
		DataLine.Info info = new DataLine.Info( TargetDataLine.class, audioFormat );

		try
		{
			// Get the output line to write to using the given
			// sample format we just created.
			mLine = (TargetDataLine)AudioSystem.getLine( info );

			// If no exception has been thrown we open the line.
			mLine.open( audioFormat );
		}
		catch( LineUnavailableException e )
		{
			throw new Exception( "Could not open Java Sound audio line for"
			        + " the audio format " + getFormat() );
		}
	}

	/**
	 * Close down the Java sound APIs.
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
