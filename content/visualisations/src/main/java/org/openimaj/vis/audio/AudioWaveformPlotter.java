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
package org.openimaj.vis.audio;

import gnu.trove.list.array.TFloatArrayList;

import java.util.ArrayList;

import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.AudioStream;
import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.processor.AudioProcessor;
import org.openimaj.audio.samples.SampleBuffer;
import org.openimaj.image.MBFImage;
import org.openimaj.image.renderer.MBFImageRenderer;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Polygon;

/**
 * 	Utilises an audio processor to plot the audio waveform to an image. 
 * 	<p>
 * 	An internal class (AudioOverviewGenerator) can be used to generate overviews
 * 	if necessary.
 * 
 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	
 *	@created 9 Jun 2011
 */
public class AudioWaveformPlotter
{
	/**
	 * 	Generates an audio overview. This is a lower-resolution version of
	 * 	the audio waveform. It takes the maximum value from a set of
	 * 	values and stores this as the overview. By default the processor
	 * 	takes the maximum value from every 5000 samples.  The method
	 * 	{@link #getAudioOverview(int, int)} allows resampling of that 
	 * 	overview.
	 *
	 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *  @created 21 Jul 2011
	 *	
	 */
	public class AudioOverviewGenerator extends AudioProcessor
	{
    	/** Number of bins in the overview */
    	private int nSamplesPerBin = -1;

    	/** The maximum in the current bin for each channel */
    	private float[] channelMax = null;
    	
    	/** The number of samples so far in the current bin being processed */
    	private int nSamplesInBin = 0;
    	
    	/** The overview data */
    	private TFloatArrayList[] audioOverview = null;

    	/** The number of channels in the audio stream */
		private int nChannels = 0;
		
		/** The audio format of the samples we're processing */
		private AudioFormat af = null;
    	
    	/**
    	 * 	Constructor
    	 * 
    	 *	@param nSamplesPerBin The number of samples per bin
    	 *	@param nChannels The number of channels
    	 */
    	public AudioOverviewGenerator( int nSamplesPerBin, int nChannels )
		{
			this.nSamplesPerBin = nSamplesPerBin;
			this.nChannels = nChannels;
			this.audioOverview = new TFloatArrayList[nChannels];
			this.channelMax = new float[nChannels];
			
			for( int i = 0; i < nChannels; i++ )
				this.audioOverview[i] = new TFloatArrayList();
		}

    	/**
    	 *	{@inheritDoc}
    	 * 	@see org.openimaj.audio.processor.AudioProcessor#process(org.openimaj.audio.SampleChunk)
    	 */
		@Override
		public SampleChunk process( SampleChunk samples )
		{
			// Store the format of the stream
			if( af == null ) af = samples.getFormat();
			
			// Get the sample data
			SampleBuffer b = samples.getSampleBuffer();
			
			// The number of samples (per channel) in this sample chunk
			int nSamples = b.size() / af.getNumChannels();
			
			// Keep a running total of how many samples we've processed
			numberOfProcessedSamples += nSamples;
			
			for( int x = 0; x < nSamples; x++ )			
			{
				for( int c = 0; c < nChannels; c++ )
				{
					// Store the maximum for the current bin
					channelMax[c] = Math.max( channelMax[c], 
							b.get(x*nChannels+c) );
				}

				// If we're still within the bin
				if( nSamplesInBin < nSamplesPerBin )
					nSamplesInBin++;
				else
				{
					// We've overflowed the bin
					for( int c = 0; c < nChannels; c++ )
					{
						// Store the current bin
						audioOverview[c].add( channelMax[c] );
						channelMax[c] = Integer.MIN_VALUE;
					}
					
					// Reset for the next bin
					nSamplesInBin = 0;
				}
			}
			
			return samples;
		}
		
		/**
		 * 	@return Get the overview data.
		 */
		public TFloatArrayList[] getAudioOverview()
		{
			return this.audioOverview;
		}
		
		/**
		 * 	Refactors the overview to given another overview. If the number
		 * 	of bins specified an overview that's finer than the actual overview
		 * 	the original overview is returned. The output of this function will
		 * 	then only return an array list of nBins or less. 
		 * 
		 * 	@param channel The channel to get
		 *	@param nBins The number of bins in the overview
		 *	@return A refactors overview
		 */
		public TFloatArrayList getAudioOverview( int channel, int nBins )
		{
			if( nBins >= audioOverview[channel].size() )
				return audioOverview[channel];
			
			TFloatArrayList ii = new TFloatArrayList();
			double scalar = (double)audioOverview[channel].size() / (double)nBins;
			for( int xx = 0; xx < nBins; xx++ )
			{
				int startBin = (int)(xx * scalar);
				int endBin = (int)((xx+1) * scalar);
				float m = Integer.MIN_VALUE;
				for( int yy = startBin; yy < endBin; yy++ )
					m = Math.max( m, audioOverview[channel].get(yy) );
				ii.add( m );
			}
			return ii;
		}
		
		/**
		 * 	Returns a polygon representing the channel overview.
		 *	@param channel The channel to get the polygon for
		 *	@param mirror whether to mirror the polygon
		 *	@param width The width of the overview to return
		 *	@return A polygon
		 */
		public Polygon getChannelPolygon( int channel, boolean mirror, int width )
		{
			TFloatArrayList overview = getAudioOverview( channel, width );
			int len = overview.size();
			double scalar = width / (double)len;
			
			ArrayList<Point2d> l = new ArrayList<Point2d>();
			for( int x = 0; x < len; x++ )
				l.add( new Point2dImpl( (float)(x * scalar), overview.get(x) ) );
			
			if( mirror )
			{
				for( int x = 1; x <= len; x++ )
					l.add( new Point2dImpl( (float)((len-x)*scalar),
						-overview.get(len-x) ) );
			}
			
			// Store how long the given overview is in milliseconds
			millisecondsInView = (long)(numberOfProcessedSamples / 
					af.getSampleRateKHz());
			
			return new Polygon( l );
		}
	}
	
	/** 
	 * 	The calculation of how many milliseconds are in the last generated
	 * 	view at the resampled overview.
	 */
	public long millisecondsInView = 0;
	
	/** The number of samples that were originally read in from the stream */
	public long numberOfProcessedSamples = 0;
	
	/** The last generated view */
	public MBFImage lastGeneratedView = null;
	
	/**
	 * 	Generates a waveform image that fits within the given width and height
	 * 	and drawn in the given colour. Note that the generated image is RGBA
	 * 	so that the colours need to be 4 dimensions and may stipulate
	 * 	transparency.
	 * 
	 * 	@param a The audio to draw
	 *	@param w The width of the image to return
	 *	@param h The height of the image to return 
	 *	@param backgroundColour The background colour to draw on the image
	 *  @param colour The colour in which to draw the audio waveform.
	 *  @return The input image.
	 */
	public static MBFImage getAudioWaveformImage( final AudioStream a, 
			final int w, final int h, final Float[] backgroundColour,
			final Float[] colour  )
    {
		return new AudioWaveformPlotter().plotAudioWaveformImage( 
				a, w, h, backgroundColour, colour );
    }
	
	/**
	 * 	Generates a waveform image that fits within the given width and height
	 * 	and drawn in the given colour. Note that the generated image is RGBA
	 * 	so that the colours need to be 4 dimensions and may stipulate
	 * 	transparency.
	 * 	<p>
	 * 	If you require information about the plot afterwards you can check
	 * 	the fields that are stored within this instance. 
	 * 
	 * 	@param a The audio to draw
	 *	@param w The width of the image to return
	 *	@param h The height of the image to return 
	 *	@param backgroundColour The background colour to draw on the image
	 *  @param colour The colour in which to draw the audio waveform.
	 *  @return The input image.
	 */	
	public MBFImage plotAudioWaveformImage( final AudioStream a, 
			final int w, final int h, final Float[] backgroundColour,
			final Float[] colour  )
	{
	    // How many pixels we'll overview per pixel
	    final int nSamplesPerPixel = 500; 
	    // TODO: This is currently fixed-size but should be based on audio length 
	    
	    // Work out how high each channel will be
	    final int channelSize = h/a.getFormat().getNumChannels();
	    
	    // This is the scalar from audio amplitude to pixels
	    final double ampScalar = (double)channelSize / Integer.MAX_VALUE;
	    
	    // Create the image we're going to draw on to - RGBA
	    final MBFImage m = new MBFImage( w, h, 4 );
	    MBFImageRenderer renderer = m.createRenderer();
	    m.fill( backgroundColour );

	    try
        {
	        // Generate the audio overview
	        AudioOverviewGenerator aap = new AudioOverviewGenerator( 
	        		nSamplesPerPixel, a.getFormat().getNumChannels() );
	        aap.process( a );
	        
	        // Draw the polygon onto the image
	        float ww = 1;
	        for( int i = 0; i < a.getFormat().getNumChannels(); i++ )
	        {			
	        	Polygon p = aap.getChannelPolygon( i, true, w );			
	        	p.scaleXY( ww, -(float)ampScalar/a.getFormat().getNumChannels() );
	        	p.translate( 0f, -(float)p.minY() + channelSize*i );
	        	renderer.drawPolygonFilled( p, colour );
	        }
        }
        catch( Exception e )
        {
        	System.err.println( "WARNING: Could not process audio " +
        			"to generate the audio overview.");
	        e.printStackTrace();
        }
		
        this.lastGeneratedView = m;
		return m;
    }
}
