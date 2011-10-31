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
import org.openimaj.image.typography.hershey.HersheyFont;

/**
 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 28 Oct 2011
 */
public class AudioCaptureDemo
{
	private int sampleChunkSize = 1024;
	private FImage spectra = null;
	private final double[] Hz = {100,500,1000,5000,10000,20000,40000};
	private boolean drawFreqBands = true;
	
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
		spectra = new FImage( 800, sampleChunkSize  );
		DisplayUtilities.displayName( spectra, "spectra" );
		DisplayUtilities.positionNamed( "spectra", img.getWidth(), 0 );
		
		final JavaSoundAudioGrabber g = new JavaSoundAudioGrabber();
		final AudioFormat af = new AudioFormat( 16, 44.1, 1 );
		g.setFormat( af );
		g.setMaxBufferSize( sampleChunkSize );
		
		g.addAudioGrabberListener( new AudioGrabberListener()
		{
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
				double binSize = (s.getFormat().getSampleRateKHz()*1000) / (f.length/2);
				
				for( int i = 0; i < f.length/2; i++ )
				{
					float re = f[i*2];
					float im = f[i*2+1];
					float mag = (float)Math.log(Math.sqrt( re*re + im*im )+1)/5f;
					fft.drawLine( i, fft.getHeight(), i, fft.getHeight()-(int)(mag*fft.getHeight()), 1f );
				}
				DisplayUtilities.displayName( fft, "fft" );
				
				// -------------------------------------------------
				// Draw Spectra
				// -------------------------------------------------
				if( s.getNumberOfSamples() != sampleChunkSize )
				{
					sampleChunkSize = s.getNumberOfSamples();
					spectra = new FImage( 800, sampleChunkSize/2  );
					DisplayUtilities.displayName( spectra, "spectra" );
					DisplayUtilities.positionNamed( "spectra", img.getWidth(), 0 );
				}
				
				spectra.shiftLeftInline();
				
				// Draw the spectra
				for( int i = 0; i < f.length/2; i++ )
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
