package org.openimaj.demos.acmmm11.presentation.slides;

import java.io.IOException;
import java.net.URL;

import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.FrequencyAudioSource;
import org.openimaj.audio.FrequencyAudioSource.Listener;
import org.openimaj.audio.JavaSoundAudioGrabber;
import org.openimaj.demos.utils.slideshowframework.PictureSlide;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.typography.hershey.HersheyFont;

public class AudioOutroSlide extends PictureSlide implements Listener{

	private int sampleChunkSize = 512;
	private FImage spectra;
	
	private final double[] Hz = {100,500,1000,5000,10000,20000,40000};
	
	public AudioOutroSlide(URL picture) throws IOException {
		super(picture);
		final JavaSoundAudioGrabber xa = new JavaSoundAudioGrabber();
		xa.setFormat( new AudioFormat( 16, 96.1, 1 ) );
		xa.setMaxBufferSize( sampleChunkSize );
		new Thread( xa ).start();
		new FrequencyAudioSource(xa).addFrequencyListener(this);
	}

	@Override
	public void consumeFrequency(float[] fft, int sampleChunkSize, double binSize) {
		if( sampleChunkSize != this.sampleChunkSize )
		{
			this.sampleChunkSize = sampleChunkSize;
			spectra = new FImage( 200, sampleChunkSize/2  );
//			DisplayUtilities.displayName( spectra, "spectra" );
		}
		spectra.shiftLeftInline();
		// Draw the spectra
		for( int i = 0; i < fft.length/4; i++ )
		{
			float re = fft[i*2];
			float im = fft[i*2+1];
			float mag = (float)Math.log(Math.sqrt( re*re + im*im )+1)/6f;
			if( mag > 1 ) mag = 1;
			spectra.setPixel( spectra.getWidth()-1, spectra.getHeight()-i, mag );
		}
		
//		FImage drawSpectra = spectra.clone();
//		
//		// Draw the frequency bands
//		for( double freq : Hz )
//		{
//			int y = drawSpectra.getHeight() - (int)(freq/binSize);
//			drawSpectra.drawLine( 0, y, spectra.getWidth(), y, 0.2f );
//			drawSpectra.drawText( ""+freq+"Hz", 4, y, HersheyFont.TIMES_BOLD, 10, 0.2f );
//		}
		
		DisplayUtilities.displayName( spectra, "spectra" );
		
	}

	
	
}
