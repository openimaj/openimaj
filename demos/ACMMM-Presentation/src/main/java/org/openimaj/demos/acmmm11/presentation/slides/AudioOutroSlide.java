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
package org.openimaj.demos.acmmm11.presentation.slides;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.FrequencyAudioSource;
import org.openimaj.audio.FrequencyAudioSource.Listener;
import org.openimaj.audio.JavaSoundAudioGrabber;
import org.openimaj.content.slideshow.PictureSlide;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.pair.Pair;

/**
 * Slide showing a picture overlayed with a live spectrogram.
 * 
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class AudioOutroSlide extends PictureSlide implements Listener {

	private int sampleChunkSize = 512;
	private FImage spectra;
	private BufferedImage buf = null;
	private JavaSoundAudioGrabber xa;
	private FrequencyAudioSource source;
	
	
	/**
	 * Default constructor
	 * @param picture
	 * @throws IOException
	 */
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
			spectra = new FImage( mbfImage.getWidth(), (high-low)*blockHeight);
		}
		
		spectra.shiftLeftInplace(blockWidth);
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
		toDraw.drawImage(new MBFImage(spectra,spectra,spectra), (mbfImage.getWidth() - spectra.width)/2, mbfImage.getHeight() - spectra.height);
		this.ic.setImage(buf = ImageUtilities.createBufferedImageForDisplay( toDraw, buf ));
	}

	
	
}
