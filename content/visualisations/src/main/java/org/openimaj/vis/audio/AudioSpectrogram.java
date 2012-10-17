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
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.audio.AudioStream;
import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.analysis.FourierTransform;
import org.openimaj.audio.filters.HanningAudioProcessor;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.vis.timeline.TimelineObject;

/**
 *	A spectrogram visualisation that scrolls the audio visualisation as the
 *	audio is processed. Vertical axis of the visualisation represents frequency,
 *	horizontal represents time (newest on the right), and pixel intensity 
 *	represents frequency amplitude.	
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 19 Jul 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class AudioSpectrogram extends TimelineObject<AudioStream>
{
	/**
	 * 	A listener for when the spectragram has completed processing.
	 *
	 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *  @created 19 Jul 2012
	 *	@version $Author$, $Revision$, $Date$
	 */
	public static interface SpectragramCompleteListener
	{
		/**
		 * 	Called when the spectragram is complete
		 *	@param as The spectragram that completed.
		 */
		public void spectragramComplete( AudioSpectrogram as );
	}
	
	/** */
	private static final long serialVersionUID = 1L;
	
	/** The time of the start of the timeline object */
	private long startTime = 0;
	
	/** The length of the audio data */
	private long length = 0;
	
	/** Height of the generated view */
	private int maxHeight = 1024;
	
	/** The Fourier transformer we'll use */
	private FourierTransform fftp = new FourierTransform();
	
	/** Whether to draw the frequency bands, or not */
	private boolean drawFreqBands = true;
	
	/** The frequency bands to mark on the spectragram */
	private final double[] Hz = {100,500,1000,5000,10000,20000,40000};
	
	/** Is the processing complete */
	private boolean isComplete = false;
	
	/** The size of the FFT bins (in Hz) */
	private double binSize = 0;
	
	/** The listeners */
	private List<SpectragramCompleteListener> listeners = 
			new ArrayList<AudioSpectrogram.SpectragramCompleteListener>();
	
	/**
	 * 	Create a spectragram for the given data
	 *	@param as The data
	 */
	public AudioSpectrogram( AudioStream as )
	{
		this.data  = as;
		this.length  = this.data.getLength();
	    setPreferredSize( new Dimension( -1, 100 ) );		
	}
	
	/**
	 * 	Create a spectragram that can be added to as and when it's necessary.
	 */
	public AudioSpectrogram()
	{
	    setPreferredSize( new Dimension( -1, 100 ) );		
	}

	/**
	 * 	Add the given listener
	 *	@param l The listener
	 */
	public void addListener( SpectragramCompleteListener l )
	{
		listeners.add( l );
	}
	
	/**
	 * 	Process the given data.
	 *	@param as The data to process
	 */
	public void processStream( AudioStream as )
	{
		this.data = as;
		this.length = this.data.getLength();
		this.processStream();
	}
	
	/**
	 * 	Process the data in this spectragram processor
	 */
	public void processStream()
	{
		if( this.data == null ) 
			return;
		
		new Thread( new Runnable()
		{				
			@Override
			public void run()
			{
				HanningAudioProcessor h = new HanningAudioProcessor( maxHeight );
				
				// -------------------------------------------------
				// Draw FFT
				// -------------------------------------------------

				SampleChunk s = null;
				int c = 0;
				while( (s = data.nextSampleChunk()) != null )
				{
					// Process the FFT
					fftp.process( h.process( s ) );
					float[] f = fftp.getLastFFT()[0];
					
					// Work out where to plot the next spectra
					binSize = (s.getFormat().getSampleRateKHz()*1000) / (f.length/2);				

					// Setup the image to draw to
					if( visImage == null )
					{
						int nFrames = (int)(s.getFormat().getSampleRateKHz() * length) /
								(s.getNumberOfSamples()/s.getFormat().getNumChannels());
						visImage = new MBFImage( nFrames, maxHeight, 3 );						
					}
					
					// Draw spectra onto image
					drawSpectra( f, c );
					
					// Counting the sample chunks
					c++;
				}
				
				isComplete = true;
				
				for( SpectragramCompleteListener l : listeners )
					l.spectragramComplete( AudioSpectrogram.this );
			}
		} ).start();
	}
	
	/**
	 * 	Draw the given spectra into the image at the given x coordinate.
	 *	@param freqs The FFT output
	 *	@param x The x position to draw it at
	 */
	private void drawSpectra( float[] f, int x )
	{
		for( int i = 0; i < f.length/4; i++ )
		{
			float re = f[i*2];
			float im = f[i*2+1];
			
			float mag = (float)Math.log(Math.sqrt( re*re + im*im )+1)/6f;
			if( mag > 1 ) mag = 1;
			
			Float[] c = new Float[]{mag,mag,mag};	
			visImage.setPixel( x, visImage.getHeight()-i, c );
		}		
	}
	
	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.timeline.TimelineObject#getStartTimeMilliseconds()
	 */
	@Override
	public long getStartTimeMilliseconds()
	{
		return this.startTime;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.timeline.TimelineObject#getEndTimeMilliseconds()
	 */
	@Override
	public long getEndTimeMilliseconds()
	{
		return this.startTime + this.length;
	}
	
	/**
	 * 	Get the last generated image. Note that if the drawing thread is
	 * 	still processing, the image may not be complete.
	 *	@return The last generated image.
	 */
	public MBFImage getLastGeneratedView()
	{
		MBFImage ii = this.visImage.clone();
		if( drawFreqBands )
		{
			// Draw the frequency bands
			for( double freq : Hz )
			{
				Float[] fbc = new Float[]{0.2f,0.2f,0.2f};
				Float[] fbtc = fbc;
				int y = ii.getHeight() - 
						(int)(freq/binSize);
				
				ii.drawLine( 0, y, ii.getWidth(), y, fbc );
				ii.drawText( ""+freq+"Hz", 4, y, HersheyFont.TIMES_BOLD, 10, fbtc );
			}
		}
		return ii;
	}
	
	/**
	 * 	Returns whether the spectragram image is complete.
	 *	@return Whether the image is complete.
	 */
	public boolean isComplete()
	{
		return this.isComplete;
	}
	
	/**
	 *	{@inheritDoc}
	 * 	@see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	@Override
	public void paint( Graphics g )
	{
		if( this.visImage != null )
		{
			MBFImage ii = this.visImage.process(
					new ResizeProcessor( getWidth(), getHeight() ) );

			if( drawFreqBands )
			{
				// Draw the frequency bands
				for( double freq : Hz )
				{
					Float[] fbc = new Float[]{0.2f,0.2f,0.2f};
					Float[] fbtc = fbc;
					int y = ii.getHeight() - 
							(int)(freq/binSize);
					
					ii.drawLine( 0, y, ii.getWidth(), y, fbc );
					ii.drawText( ""+freq+"Hz", 4, y, HersheyFont.TIMES_BOLD, 10, fbtc );
				}
			}

			// Copy the vis to the Swing UI
			g.drawImage( ImageUtilities.createBufferedImage( ii ), 
					0, 0, null );
		}
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.Visualisation#update()
	 */
	@Override
	public void update()
	{
		repaint();
	}
}
