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
import org.openimaj.vis.VisualisationImpl;

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
public class AudioSpectrogram extends VisualisationImpl<float[]>
{
	/**
	 * A listener for when the spectrogram has completed processing.
	 *
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 * @created 19 Jul 2012
	 * @version $Author$, $Revision$, $Date$
	 */
	public static interface SpectrogramCompleteListener
	{
		/**
		 * Called when the spectragram is complete
		 *
		 * @param as The spectragram that completed.
		 */
		public void spectrogramComplete( AudioSpectrogram as );
	}

	/** */
	private static final long serialVersionUID = 1L;

	/** The Fourier transformer we'll use */
	private final FourierTransform fftp = new FourierTransform();

	/** Whether to draw the frequency bands, or not */
	private boolean drawFreqBands = true;

	/** The frequency bands to mark on the spectragram */
	private double[] frequencyBands =
	{ 100, 500, 1000, 5000, 10000, 20000, 40000 };

	/** Is the processing complete */
	private boolean isComplete = false;

	/** The size of the FFT bins (in Hz) */
	private double binSize = 0;

	/** The listeners */
	private final List<SpectrogramCompleteListener> listeners =
			new ArrayList<AudioSpectrogram.SpectrogramCompleteListener>();

	/** The format of the audio being processed */
	private AudioFormat audioFormat = null;

	/** Whether to draw the line at the current position of drawing */
	private boolean drawCurrentPositionLine = true;

	/** Colour of the line delineating the end of the current spectrogram */
	private Float[] currentPositionLineColour = new Float[]
			{ 0.5f, 0.5f, 0.5f, 1f };

	/** The total number of frames we've drawn */
	private int nFrames = 0;

	/** The current position we're drawing at */
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
	public void addListener( final SpectrogramCompleteListener l )
	{
		this.listeners.add( l );
	}

	/**
	 * 	Remove the given listener
	 *	@param l The listener to remove
	 */
	public void removeListener( final SpectrogramCompleteListener l )
	{
		this.listeners.remove( l );
	}

	/**
	 * Process the entire stream (or as much data will fit into the
	 * visualisation window). The last transform to be processed will
	 * be available in the <code>data</code> field of this class.
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
				// We'll process the incoming stream with a hanning processor
				final HanningAudioProcessor hanningProcessor = new HanningAudioProcessor(
						as, AudioSpectrogram.this.visImage.getHeight()*8 );

				// Loop through all the sample chunks and process them.
				SampleChunk s = null;
				AudioSpectrogram.this.currentDrawPosition = 0;
				while( (s = hanningProcessor.nextSampleChunk()) != null
						&& AudioSpectrogram.this.currentDrawPosition < AudioSpectrogram.this.visImage.getWidth() )
				{
					AudioSpectrogram.this.process( s );
				}

				// We're done.
				AudioSpectrogram.this.isComplete = true;

				// So, fire the complete listener.
				for( final SpectrogramCompleteListener l : AudioSpectrogram.this.listeners )
					l.spectrogramComplete( AudioSpectrogram.this );
			}
		} ).start();
	}

	/**
	 * 	Processes a single sample chunk: calculates the FFT, gets the magnitudes,
	 * 	copies the format (if it's the first chunk), and then goes on to update the image.
	 *
	 * 	@param s The sample chunk to process
	 */
	public void process( final SampleChunk s )
	{
		// Process the FFT
		this.fftp.process( s.getSampleBuffer() );

		// Get the magnitudes to show in the spectrogram
		final float[] f = this.fftp.getNormalisedMagnitudes( 1f/Integer.MAX_VALUE )[0];

		// Store the format of this sample chunk if we don't have one yet.
		// This allows us to continue to draw the frequency bands on the image
		// (if it's configured to do that).
		if( this.audioFormat == null )
			this.audioFormat = s.getFormat().clone();

		// Store this FFT into the data member. Note this calls a method in this class.
		this.setData( f );
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.VisualisationImpl#setData(java.lang.Object)
	 */
	@Override
	public void setData( final float[] data )
	{
		// Set the data into the data field
		super.setData( data );

		// We count the number of frames so we can stop
		// if we get to the edge of the window
		this.nFrames++;

		// Shift the data along (if the window's too small)
		this.shiftData();

		// Repaint the visualisation
		this.updateVis();
	}

	/**
	 * 	Add the given sample chunk into the spectrogram. This is a
	 * 	handy method for processing an audio stream outside of this class.
	 *
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

	/**
	 * 	Checks whether the visualisation needs to be shifted to the left.
	 * 	If not it draws the spectra from the left of the image. If it
	 * 	reaches the right of the image, the image will be scrolled to the
	 * 	left and the new spectra drawn at the right of the image.
	 */
	private void shiftData()
	{
		// Check if we should be drawing outside of the image. If so,
		// shift the image to the left and draw at the right hand edge.
		// (our draw position is given in currentDrawPosition and it's
		// not updated if we enter this if clause).
		if( this.nFrames > this.visImage.getWidth() )
			this.previousSpecImage = this.previousSpecImage.shiftLeft();
		else
		{
			// Blat the previous spectrogram and update where we're going to draw
			// the newest spectra.
			if( this.nFrames > 0 )
			{
				final FImage t = new FImage( this.nFrames, this.visImage.getHeight() );
				if( this.previousSpecImage != null )
					t.drawImage( this.previousSpecImage, 0, t.getHeight()-this.previousSpecImage.getHeight() );
				this.previousSpecImage = t;
				this.currentDrawPosition++;
			}
		}

		// Draw the newest spectra. Note that we draw onto the "previousSpecImage"
		// memory image and we blat this out in the update() method.
		synchronized( this.data )
		{
			// Draw spectra onto image
			this.drawSpectra( this.previousSpecImage, this.data, this.currentDrawPosition-1 );
		}
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.VisualisationImpl#update()
	 */
	@Override
	public void update()
	{
		if( this.data != null )
		{
			synchronized( this.visImage )
			{
				// Draw the spectra image if we have one.
				if( this.previousSpecImage != null )
				{
					this.visImage.drawImage( MBFImage.createRGB(this.previousSpecImage), 0,
						this.visImage.getHeight()-this.previousSpecImage.getHeight() );
				}

				// Draw the frequency bands onto the image.
				if( this.drawFreqBands && this.audioFormat != null )
				{
					// Work out where to plot the next spectra
					this.binSize = (this.audioFormat.getSampleRateKHz() * 500) / this.data.length;

					// Draw the frequency bands
					for( final double freq : this.frequencyBands )
					{
						final Float[] fbc = new Float[] { 0.2f, 0.2f, 0.2f };
						final Float[] fbtc = fbc;
						final int y = (int)(this.visImage.getHeight() -
								freq/this.binSize);

						this.visImage.drawLine( 0, y, this.visImage.getWidth(), y, fbc );
						this.visImage.drawText( "" + freq + "Hz", 4, y, HersheyFont.TIMES_BOLD, 10, fbtc );
					}
				}

				// Draw the bar showing where we're drawing at.
				if( this.drawCurrentPositionLine )
					this.visImage.drawLine( this.currentDrawPosition + 1, 0,
							this.currentDrawPosition + 1, this.visImage.getHeight(),
							this.currentPositionLineColour );
			}
		}
	}

	/**
	 *	Returns whether the frequency bands are being drawn.
	 *	@return TRUE if the bands are being drawn; FALSE otherwise
	 */
	public boolean isDrawFreqBands()
	{
		return this.drawFreqBands;
	}

	/**
	 * 	Set whether to overlay the frequency bands onto the image.
	 *	@param drawFreqBands TRUE to draw the frequency bands; FALSE otherwise
	 */
	public void setDrawFreqBands( final boolean drawFreqBands )
	{
		this.drawFreqBands = drawFreqBands;
	}

	/**
	 * 	Get the frequency bands which are being drawn in Hz.
	 *	@return The frequency bands which are being drawn.
	 */
	public double[] getFrequencyBands()
	{
		return this.frequencyBands;
	}

	/**
	 * 	Set the frequency bands to overlay on the image in Hz.
	 *	@param frequencyBands the frequency bands to draw
	 */
	public void setFrequencyBands( final double[] frequencyBands )
	{
		this.frequencyBands = frequencyBands;
	}

	/**
	 * 	Returns whether the current position line is being drawn
	 *	@return Whether the current position line is being drawn
	 */
	public boolean isDrawCurrentPositionLine()
	{
		return this.drawCurrentPositionLine;
	}

	/**
	 * 	Set whether to draw the position at which the current spectra will be drawn
	 *	@param drawCurrentPositionLine TRUE to draw the current position line
	 */
	public void setDrawCurrentPositionLine( final boolean drawCurrentPositionLine )
	{
		this.drawCurrentPositionLine = drawCurrentPositionLine;
	}

	/**
	 * 	Get the colour of the current position line
	 *	@return The current position line colour
	 */
	public Float[] getCurrentPositionLineColour()
	{
		return this.currentPositionLineColour;
	}

	/**
	 * 	Set the colour of the line which is showning the current draw position.
	 *	@param currentPositionLineColour The colour of the current draw position.
	 */
	public void setCurrentPositionLineColour( final Float[] currentPositionLineColour )
	{
		this.currentPositionLineColour = currentPositionLineColour;
	}

	/**
	 *	Get the position at which the next spectrum will be drawn.
	 *	@return the position at which the next spectrum will be drawn.
	 */
	public int getCurrentDrawPosition()
	{
		return this.currentDrawPosition;
	}
}
