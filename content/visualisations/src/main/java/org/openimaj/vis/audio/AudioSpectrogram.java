/**
 * Copyright (c) 2011, The University of Southampton and the individual
 * contributors. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * * Neither the name of the University of Southampton nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
/**
 *
 */
package org.openimaj.vis.audio;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.AudioStream;
import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.analysis.FourierTransform;
import org.openimaj.audio.filters.HanningAudioProcessor;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.vis.Visualisation;

/**
 * A spectrogram visualisation that scrolls the audio visualisation as the audio
 * is processed. Vertical axis of the visualisation represents frequency,
 * horizontal represents time (newest on the right), and pixel intensity
 * represents frequency amplitude.
 *
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 19 Jul 2012
 * @version $Author$, $Revision$, $Date$
 */
public class AudioSpectrogram extends Visualisation<float[]>
{
	/**
	 * A listener for when the spectragram has completed processing.
	 *
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 * @created 19 Jul 2012
	 * @version $Author$, $Revision$, $Date$
	 */
	public static interface SpectragramCompleteListener
	{
		/**
		 * Called when the spectragram is complete
		 *
		 * @param as The spectragram that completed.
		 */
		public void spectragramComplete( AudioSpectrogram as );
	}

	/** */
	private static final long serialVersionUID = 1L;

	/** The Fourier transformer we'll use */
	private final FourierTransform fftp = new FourierTransform();

	/** Whether to draw the frequency bands, or not */
	private final boolean drawFreqBands = true;

	/** The frequency bands to mark on the spectragram */
	private final double[] Hz =
	{ 100, 500, 1000, 5000, 10000, 20000, 40000 };

	/** Is the processing complete */
	private boolean isComplete = false;

	/** The size of the FFT bins (in Hz) */
	private double binSize = 0;

	/** The listeners */
	private final List<SpectragramCompleteListener> listeners = new ArrayList<AudioSpectrogram.SpectragramCompleteListener>();

	/** The format of the audio being processed */
	private AudioFormat audioFormat = null;

	/** Whether to draw the line at the current position of drawing */
	private final boolean drawCurrentPositionLine = true;

	/** Colour of the line delineating the end of the current spectrogram */
	private final Float[] currentPositionLineColour = new Float[]
	{ 0.5f, 0.5f, 0.5f, 1f };

	private int nFrames = 0;

	private int currentDrawPosition;

	/** The last spectrogram image */
	private FImage previousSpecImage = null;

	/**
	 * Create a spectrogram that can be added to as and when it's necessary.
	 */
	public AudioSpectrogram()
	{
		this.setPreferredSize( new Dimension( -1, 100 ) );
		super.clearBeforeDraw = true;
	}

	/**
	 * Construct a visualisation of the given size
	 *
	 * @param w Width of the required visualisation
	 * @param h Height of the required visualisation
	 */
	public AudioSpectrogram( final int w, final int h )
	{
		super( w, h );
		super.clearBeforeDraw = true;
		this.setPreferredSize( new Dimension( w, h ) );
	}

	/**
	 * Add the given listener
	 *
	 * @param l The listener
	 */
	public void addListener( final SpectragramCompleteListener l )
	{
		this.listeners.add( l );
	}

	/**
	 * Process the entire stream (or as much data will fit into the
	 * visualisation window) and will store all the processed
	 * {@link SampleChunk} into the data member of the visualisation.
	 *
	 * @param as The stream to process
	 */
	public void processStream( final AudioStream as )
	{
		this.audioFormat = as.getFormat().clone();
		new Thread( new Runnable()
		{
			@Override
			public void run()
			{
				/** The hanning processor */
				final HanningAudioProcessor hanningProcessor = new HanningAudioProcessor(
						as, AudioSpectrogram.this.visImage.getHeight()*8 );

				SampleChunk s = null;
				AudioSpectrogram.this.currentDrawPosition = 0;
				while( (s = hanningProcessor.nextSampleChunk()) != null
						&& AudioSpectrogram.this.currentDrawPosition < AudioSpectrogram.this.visImage.getWidth() )
				{
					AudioSpectrogram.this.process( s );
				}

				AudioSpectrogram.this.isComplete = true;

				for( final SpectragramCompleteListener l : AudioSpectrogram.this.listeners )
					l.spectragramComplete( AudioSpectrogram.this );
			}
		} ).start();
	}

	/**
	 * @param s The sample chunk
	 */
	public void process( final SampleChunk s )
	{
		// Process the FFT
		this.fftp.process( s.getSampleBuffer() );
		final float[] f = this.fftp.getNormalisedMagnitudes( 1f/Integer.MAX_VALUE )[0];

		if( this.audioFormat == null )
			this.audioFormat = s.getFormat().clone();

		// Store this FFT into the data
		this.setData( f );
	}

	@Override
	public void setData( final float[] data )
	{
		super.setData( data );
		this.nFrames++;

		this.shiftData();
		this.updateVis();
	}

	/**
	 * 	Add the given sample chunk into the spectrogram.
	 *	@param sc The sample chunk to add.
	 */
	public void setData( final SampleChunk sc )
	{
		this.process( sc );
	}

	/**
	 * Draw the given spectra into the image at the given x coordinate.
	 *
	 * @param freqs The FFT output
	 * @param x The x position to draw it at
	 */
	private void drawSpectra( final FImage img, final float[] f, final int x )
	{
		if( img == null || f == null ) return;

//		final double ps = img.getHeight()/f.length;
		for( int i = 0; i < f.length; i++ )
		{
//			img.drawLine( x, img.getHeight()-i, x, (int)(img.getHeight()-i-ps), mag );
			final int y = img.getHeight() - i -1;
			img.setPixel( x, y, f[i] );
		}
	}

	/**
	 * Returns whether the spectragram image is complete.
	 *
	 * @return Whether the image is complete.
	 */
	public boolean isComplete()
	{
		return this.isComplete;
	}

	private void shiftData()
	{
		if( this.nFrames > this.visImage.getWidth() )
			this.previousSpecImage = this.previousSpecImage.shiftLeft();
		else
		{
			if( this.nFrames > 0 )
			{
				final FImage t = new FImage( this.nFrames, this.visImage.getHeight() );
				if( this.previousSpecImage != null )
					t.drawImage( this.previousSpecImage, 0, t.getHeight()-this.previousSpecImage.getHeight() );
				this.previousSpecImage = t;
				this.currentDrawPosition++;
			}
		}
		synchronized( this.data )
		{
			// Draw spectra onto image
			this.drawSpectra( this.previousSpecImage, this.data, this.currentDrawPosition-1 );
		}
	}

	@Override
	public void update()
	{
		if( this.data != null )
		{
			synchronized( this.visImage )
			{
				if( this.previousSpecImage != null )
				{
					this.visImage.drawImage( MBFImage.createRGB(this.previousSpecImage), 0,
						this.visImage.getHeight()-this.previousSpecImage.getHeight() );
				}

				if( this.drawFreqBands && this.audioFormat != null )
				{
					// Work out where to plot the next spectra
					this.binSize = (this.audioFormat.getSampleRateKHz() * 500) / this.data.length;

					// Draw the frequency bands
					for( final double freq : this.Hz )
					{
						final Float[] fbc = new Float[] { 0.2f, 0.2f, 0.2f };
						final Float[] fbtc = fbc;
						final int y = (int)(this.visImage.getHeight() -
								freq/this.binSize);

						this.visImage.drawLine( 0, y, this.visImage.getWidth(), y, fbc );
						this.visImage.drawText( "" + freq + "Hz", 4, y, HersheyFont.TIMES_BOLD, 10, fbtc );
					}
				}

				if( this.drawCurrentPositionLine )
					this.visImage.drawLine( this.currentDrawPosition + 1, 0, this.currentDrawPosition + 1, this.visImage.getHeight(),
							this.currentPositionLineColour );
			}
		}
	}
}
