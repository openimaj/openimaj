/**
 * 
 */
package org.openimaj.demos;

import java.nio.ShortBuffer;

import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.AudioGrabberListener;
import org.openimaj.audio.FourierTransform;
import org.openimaj.audio.JavaSoundAudioGrabber;
import org.openimaj.audio.SampleChunk;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;

/**
 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 28 Oct 2011
 */
public class AudioCaptureDemo
{
	/**
	 * 
	 */
	public AudioCaptureDemo()
    {
		final FImage img = new FImage( 512, 400 );
		DisplayUtilities.displayName( img, "display" );
		final FImage fft = new FImage( img.getWidth(), 400 );
		DisplayUtilities.displayName( fft, "fft" );
		DisplayUtilities.positionNamed( "fft", 0, img.getHeight() );
		final FourierTransform fftp = new FourierTransform();
		final FImage spectra = new FImage( 800, img.getHeight()+img.getWidth() );
		DisplayUtilities.displayName( spectra, "spectra" );
		DisplayUtilities.positionNamed( "spectra", img.getWidth(), 0 );
		
		final JavaSoundAudioGrabber g = new JavaSoundAudioGrabber();
		final AudioFormat af = new AudioFormat( 16, 22.05, 1 );
		g.setFormat( af );
		g.setMaxBufferSize( 1024 );
		
		g.addAudioGrabberListener( new AudioGrabberListener()
		{
			public int pos = 0;
			
			@Override
			public void samplesAvailable( SampleChunk s )
			{
				final ShortBuffer sb = s.getSamplesAsByteBuffer().asShortBuffer();
				
				// -------------------------------------------------
				// Draw waveform
				// -------------------------------------------------
				img.zero();
				for( int i = 1; i < 1000; i++ )
					img.drawLine( 
							i-1, sb.get((i-1)*af.getNumChannels())/128+200, 
							i,   sb.get(i*af.getNumChannels())/128+200, 1f );
				DisplayUtilities.displayName( img, "display" );

				// -------------------------------------------------
				// Draw FFT
				// -------------------------------------------------
				fft.zero();
				fftp.process( s );
				float[] f = fftp.getLastFFT();
				for( int i = 0; i < f.length/2; i++ )
				{
					float re = f[i];
					float im = f[i*2];
					float mag = (float)Math.log(Math.sqrt( re*re + im*im )+1)/5f;
					fft.drawLine( i, fft.getHeight(), i, fft.getHeight()-(int)(mag*fft.getHeight()), 1f );
				}
				DisplayUtilities.displayName( fft, "fft" );
				
				// -------------------------------------------------
				// Draw Spectra
				// -------------------------------------------------
				for( int i = 0; i < f.length/2; i++ )
				{
					float re = f[i];
					float im = f[i*2];
					float mag = (float)Math.log(Math.sqrt( re*re + im*im )+1)/5f;
					spectra.setPixel( pos, spectra.getHeight()-i, mag );
				}
				pos++;
				pos %= spectra.getWidth();
				DisplayUtilities.displayName( spectra, "spectra" );
			}
		});
		
		g.run();
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
