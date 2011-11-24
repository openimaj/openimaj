package org.openimaj.demos.acmmm11.presentation.slides;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.FrequencyAudioSource;
import org.openimaj.audio.FrequencyAudioSource.Listener;
import org.openimaj.audio.JavaSoundAudioGrabber;
import org.openimaj.demos.utils.slideshowframework.PictureSlide;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.pair.Pair;

public class AudioOutroSlide extends PictureSlide implements Listener{

	private int sampleChunkSize = 512;
	private FImage spectra;
	private BufferedImage buf = null;
	private JavaSoundAudioGrabber xa;
	private FrequencyAudioSource source;
	
	
	public AudioOutroSlide(URL picture) throws IOException {
		super(picture);
	}
	
	@Override
	public Component getComponent(int width, int height) throws IOException{
		Component comp = super.getComponent(width, height);
		xa = new JavaSoundAudioGrabber();
		xa.setFormat( new AudioFormat( 16, 96.1, 1 ) );
		xa.setMaxBufferSize( sampleChunkSize );
		new Thread( xa ).start();
		source = new FrequencyAudioSource(xa);
		source.addFrequencyListener(this,new Pair<Integer>(30,3400));
		spectra = null;
		return comp;
	}
	
	@Override
	public void close(){
		super.close();
		if(xa!=null){
			
			xa.stop();
			xa = null;
		}
	}

	@Override
	public void consumeFrequency(float[] fftReal, float[] fftImag,int low,int high) {
		
		int blockWidth = 10;
		int blockHeight = 5;
		
		if( spectra == null || spectra.getHeight() != (high-low) * blockHeight )
		{
			spectra = new FImage( 800, (high-low)*blockHeight);
		}
		
		spectra.shiftLeftInline(blockWidth);
		// Draw the spectra
		for( int i = low; i < high; i++ )
		{
			float re = fftReal[i];
			float im = fftImag[i];
			float mag = (float)Math.log(Math.sqrt( re*re + im*im )+1)/5;
			if( mag > 1 ) mag = 1;
			spectra.drawShapeFilled(new Rectangle(spectra.getWidth()-blockWidth, spectra.getHeight()-(i * blockHeight), blockWidth,blockHeight), mag );
		}
		
		MBFImage toDraw = mbfImage.clone();
		toDraw.drawImage(new MBFImage(spectra,spectra,spectra), (mbfImage.getWidth() - spectra.width)/2, 20);
		this.ic.setImage(buf = ImageUtilities.createBufferedImageForDisplay( toDraw, buf ));
	}

	
	
}
