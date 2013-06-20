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

	private final int sampleChunkSize = 512;
	private FImage spectra;
	private BufferedImage buf = null;
	private JavaSoundAudioGrabber xa;
	private FrequencyAudioSource source;


	/**
	 * Default constructor
	 * @param picture
	 * @throws IOException
	 */
	public AudioOutroSlide(final URL picture) throws IOException {
		super(picture);
	}

	@Override
	public Component getComponent(final int width, final int height) throws IOException{
		final Component comp = super.getComponent(width, height);
		this.xa = new JavaSoundAudioGrabber(new AudioFormat( 16, 96.1, 1 ));
		this.xa.setMaxBufferSize( this.sampleChunkSize );
		new Thread( this.xa ).start();
		this.source = new FrequencyAudioSource(this.xa);
		this.source.addFrequencyListener(this,new Pair<Integer>(30,3400));
		this.spectra = null;
		return comp;
	}

	@Override
	public void close(){
		super.close();
		if(this.xa!=null){

			this.xa.stop();
			this.xa = null;
		}
	}

	@Override
	public void consumeFrequency(final float[] fftReal, final float[] fftImag,final int low,final int high) {

		final int blockWidth = 10;
		final int blockHeight = 5;

		if( this.spectra == null || this.spectra.getHeight() != (high-low) * blockHeight )
		{
			this.spectra = new FImage( this.mbfImage.getWidth(), (high-low)*blockHeight);
		}

		this.spectra.shiftLeftInplace(blockWidth);
		// Draw the spectra
		for( int i = low; i < high; i++ )
		{
			final float re = fftReal[i];
			final float im = fftImag[i];
			float mag = (float)Math.log(Math.sqrt( re*re + im*im )+1)/5;
			if( mag > 1 ) mag = 1;
			this.spectra.drawShapeFilled(new Rectangle(this.spectra.getWidth()-blockWidth, this.spectra.getHeight()-(i * blockHeight), blockWidth,blockHeight), mag );
		}

		final MBFImage toDraw = this.mbfImage.clone();
		toDraw.drawImage(new MBFImage(this.spectra,this.spectra,this.spectra), (this.mbfImage.getWidth() - this.spectra.width)/2, this.mbfImage.getHeight() - this.spectra.height);
		this.ic.setImage(this.buf = ImageUtilities.createBufferedImageForDisplay( toDraw, this.buf ));
	}



}
