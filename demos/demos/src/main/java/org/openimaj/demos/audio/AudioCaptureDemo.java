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
package org.openimaj.demos.audio;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.FourierTransform;
import org.openimaj.audio.JavaSoundAudioGrabber;
import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.filters.HanningAudioProcessor;
import org.openimaj.demos.Demo;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.typography.hershey.HersheyFont;

/**
 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	
 *	@created 28 Oct 2011
 */
@Demo(
	title = "Audio Spectrum Processing",
	author = "David Dupplaw", 
	description = "Demonstrates the basic FFT audio processing in OpenIMAJ",
	keywords = { "audio", "fft", "spectra" },
	icon = "/org/openimaj/demos/icons/audio/microphone-icon.png"
)
public class AudioCaptureDemo
{
	/** We'll at first ask for a sample chunk size of 1024. We might not get it */
	private int sampleChunkSize = 512;
	
	/** The image displaying the waveform */
	private FImage img = null;
	
	/** The image displaying the FFT bins */
	private FImage fft = null;
	
	/** The image displaying the spectragram */
	private FImage spectra = null;
	
	/** The frequency bands to mark on the spectragram */
	private final double[] Hz = {100,500,1000,5000,10000,20000,40000};
	
	/** Whether to mark the frequency bands on the spectragram */
	private boolean drawFreqBands = true;
	
	/** The Fourier transform processor we're going to use */
	private FourierTransform fftp = null;
	
	/**
	 * 
	 */
	public AudioCaptureDemo()
    {
		img = new FImage( 512, 400 );
		DisplayUtilities.displayName( img, "display" );
		
		fft = new FImage( img.getWidth(), 400 );
		DisplayUtilities.displayName( fft, "fft" );
		DisplayUtilities.positionNamed( "fft", 0, img.getHeight() );
		
		fftp = new FourierTransform();
		spectra = new FImage( 800, sampleChunkSize/2  );
		DisplayUtilities.displayName( spectra, "spectra", true );
		DisplayUtilities.positionNamed( "spectra", img.getWidth(), 0 );
		
		// Uncomment the below to read from a file
//		final XuggleAudio xa = new XuggleAudio( AudioCaptureDemo.class.
//			getResource("/org/openimaj/demos/audio/140bpm-Arp.mp3" ) );

		// Uncomment the below for grabbing audio live
		final JavaSoundAudioGrabber xa = new JavaSoundAudioGrabber();
		xa.setFormat( new AudioFormat( 16, 96.1, 1 ) );
		xa.setMaxBufferSize( sampleChunkSize );
		new Thread( xa ).start();

		// Hanning processor on top of the main audio stream
		HanningAudioProcessor g = 
			new HanningAudioProcessor( xa, img.getWidth()*xa.getFormat().getNumChannels() )
			{
				@Override
				public SampleChunk process( SampleChunk sample )
				{
					updateDisplay( sample );
					return sample;
				}
			};

		System.out.println( "Using audio stream: "+g.getFormat() );
		
		try
        {
	        Thread.sleep( 500 );
	        SampleChunk s = null;
	        while( (s = g.nextSampleChunk()) != null )
	        	updateDisplay( s );
        }
        catch( InterruptedException e )
        {
	        e.printStackTrace();
        }
    }

	/**
	 * 	Updates the visualisation each time a sample chunk comes in.
	 *  @param s The sample chunk to display
	 */
	public void updateDisplay( SampleChunk s )
	{
		ShortBuffer sb = null;
		ByteBuffer  bb = null;
		if( (bb = s.getSamplesAsByteBuffer()) != null )
				sb = bb.asShortBuffer();
		else	return;
		
		// -------------------------------------------------
		// Draw waveform
		// -------------------------------------------------
		img.zero();
		final int yOffset = img.getHeight()/2;
		for( int i = 1; i < s.getNumberOfSamples()/s.getFormat().getNumChannels(); i++ )
		{
			img.drawLine( 
				i-1, sb.get( (i-1)*s.getFormat().getNumChannels() )/256+yOffset, 
				  i, sb.get( i*s.getFormat().getNumChannels() )/256+yOffset, 1f );
		}
		DisplayUtilities.displayName( img, "display" );

		// -------------------------------------------------
		// Draw FFT
		// -------------------------------------------------
		fft.zero();				
		fftp.process( s );
		
		float[] f = fftp.getLastFFT();
		double binSize = (s.getFormat().getSampleRateKHz()*1000) / (f.length/2);
		
		for( int i = 0; i < f.length/4; i++ )
		{
			float re = f[i*2];
			float im = f[i*2+1];
			float mag = (float)Math.log(Math.sqrt( re*re + im*im )+1)/5f;
			fft.drawLine( i*2, fft.getHeight(), i*2, fft.getHeight()-(int)(mag*fft.getHeight()), 2, 1f );
		}
		DisplayUtilities.displayName( fft, "fft" );
		
		// -------------------------------------------------
		// Draw Spectra
		// -------------------------------------------------
//		System.out.println( "Sample chunk size: "+sampleChunkSize );
//		System.out.println( "Number of samples: "+s.getNumberOfSamples() );
//		System.out.println( "FFT size: "+f.length );
		if( s.getNumberOfSamples() != sampleChunkSize )
		{
			sampleChunkSize = s.getNumberOfSamples();
			spectra = new FImage( 800, sampleChunkSize/2  );
			DisplayUtilities.displayName( spectra, "spectra" );
			DisplayUtilities.positionNamed( "spectra", img.getWidth(), 0 );
		}
		
		spectra.shiftLeftInplace();
		
		// Draw the spectra
		for( int i = 0; i < f.length/4; i++ )
		{
			float re = f[i*2];
			float im = f[i*2+1];
			float mag = (float)Math.log(Math.sqrt( re*re + im*im )+1)/6f;
			if( mag > 1 ) mag = 1;
			spectra.setPixel( spectra.getWidth()-1, spectra.getHeight()-i, mag );
		}

		FImage drawSpectra = spectra;
		if( drawFreqBands )
		{
			drawSpectra = spectra.clone();
			
			// Draw the frequency bands
			for( double freq : Hz )
			{
				int y = drawSpectra.getHeight() - (int)(freq/binSize);
				drawSpectra.drawLine( 0, y, spectra.getWidth(), y, 0.2f );
				drawSpectra.drawText( ""+freq+"Hz", 4, y, HersheyFont.TIMES_BOLD, 10, 0.2f );
			}
		}
		
		DisplayUtilities.displayName( drawSpectra, "spectra" );
	}
	
	/**
	 * 
	 *  @param args
	 */
	public static void main( String[] args )
    {
	    new AudioCaptureDemo();
    }
}
