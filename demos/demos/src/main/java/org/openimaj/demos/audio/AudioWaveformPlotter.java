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
package org.openimaj.demos.audio;

import gnu.trove.TIntArrayList;

import java.io.File;
import java.io.IOException;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.processor.AudioProcessor;
import org.openimaj.demos.Demo;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processor.PixelProcessor;
import org.openimaj.image.renderer.MBFImageRenderer;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.video.xuggle.XuggleAudio;

/**
 * 	Utilises an audio processor to plot the audio waveform.
 * 
 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 9 Jun 2011
 */
@Demo(
	author = "David Dupplaw", 
	description = "Demonstrates reading an audio file and plotting the waveform" +
			" into an image.", 
	keywords = { "audio", "image", "waveform" }, 
	title = "Audio Waveform Plotter",
	icon = "/org/openimaj/demos/icons/audio/audio-waveform-icon.png"
)
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
	 *	@author David Dupplaw <dpd@ecs.soton.ac.uk>
	 *  @created 21 Jul 2011
	 *	@version $Author$, $Revision$, $Date$
	 */
	public static class AudioOverviewGenerator extends AudioProcessor
	{
    	/** Number of bins in the overview */
    	private int nSamplesPerBin = 5000;

    	/** The maximum in the current bin for each channel */
    	private int[] channelMax = null;
    	
    	/** The number of samples so far in the current bin being processed */
    	private int nSamplesInBin = 0;
    	
    	/** The overview data */
    	private TIntArrayList[] audioOverview = null;

    	/** The number of channels in the audio stream */
		private int nChannels = 0;
    	
    	/**
    	 * 
    	 *	@param nSamplesPerBin
    	 *	@param nChannels
    	 */
    	public AudioOverviewGenerator( int nSamplesPerBin, int nChannels )
		{
			this.nSamplesPerBin = nSamplesPerBin;
			this.nChannels = nChannels;
			this.audioOverview = new TIntArrayList[nChannels];
			this.channelMax = new int[nChannels];
			
			for( int i = 0; i < nChannels; i++ )
				this.audioOverview[i] = new TIntArrayList();
		}

    	/**
    	 *	@inheritDoc
    	 * 	@see org.openimaj.audio.processor.AudioProcessor#process(org.openimaj.audio.SampleChunk)
    	 */
		@Override
		public SampleChunk process( SampleChunk samples )
		{
			// The number of samples (per channel) in this sample chunk
			int nSamples = samples.getSamples().length/nChannels/2;
			
			// Get the sample data
			ShortBuffer b = samples.getSamplesAsByteBuffer().asShortBuffer();
			
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
		 * 	Get the overview data.
		 *	@return
		 */
		public TIntArrayList[] getAudioOverview()
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
		public TIntArrayList getAudioOverview( int channel, int nBins )
		{
			System.out.println( "Refactoring overview of channel "+channel
					+" to "+nBins+" bins from "+audioOverview[channel].size() );
			
			if( nBins > audioOverview[channel].size() )
				return audioOverview[channel];
			
			TIntArrayList ii = new TIntArrayList();
			double scalar = (double)audioOverview[channel].size() / (double)nBins;
			for( int xx = 0; xx < nBins; xx++ )
			{
				int startBin = (int)(xx * scalar);
				int endBin = (int)((xx+1) * scalar);
				int m = Integer.MIN_VALUE;
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
			TIntArrayList overview = getAudioOverview( channel, width );
			int len = overview.size();
			
			ArrayList<Point2d> l = new ArrayList<Point2d>();
			for( int x = 0; x < len; x++ )
				l.add( new Point2dImpl( x, overview.get(x) ) );
			
			if( mirror )
			{
				for( int x = 1; x <= len; x++ )
					l.add( new Point2dImpl( len-x,
						-overview.get(len-x) ) );
			}
			
			return new Polygon( l );
		}
	}
	
	public static void drawLinesToPolygon( Polygon p, MBFImageRenderer renderer, 
			Float[] col, int y, int w )
	{
		for( Point2d pp : p )
			renderer.drawLine( (int)pp.getX(), y, (int)pp.getX(), 
					(int)pp.getY(), w, col );
	}
	
	public static void main( String[] args )
    {
		// Open the audio stream
	    final XuggleAudio a = new XuggleAudio( 
	    		new File( "src/test/resources/glen.mp3") );
	    
	    // This is how wide we're going to draw the display
	    final int w = 1920;
	    
	    // This is how high we'll draw the display
	    final int h = 200;
	    
	    // How many pixels we'll overview per pixel
	    final int nSamplesPerPixel = 2000; // TODO: This is currently fixed
	    
	    // Work out how high each channel will be
	    final int channelSize = h/a.getFormat().getNumChannels();
	    
	    // This is the scalar from audio amplitude to pixels
	    final double ampScalar = (double)channelSize / Short.MAX_VALUE;
	    
	    System.out.println( "Samples per pixel: "+nSamplesPerPixel );
	    System.out.println( "Channel height: "+channelSize );
	    System.out.println( "Amplitude scalar: "+ampScalar );
	    
	    // Create the image we're going to draw on to - RGBA
	    final MBFImage m = new MBFImage( w, h, 4 );
	    MBFImageRenderer renderer = m.createRenderer();
	    m.fill( new Float[]{0f,0f,0f,1f} );

	    // Generate the audio overview
	    System.out.println( "Processing audio..." );
	    AudioOverviewGenerator aap = new AudioOverviewGenerator( 
	    		nSamplesPerPixel, a.getFormat().getNumChannels() );
		aap.process( a );
		
		// Draw the polygon onto the image
		float ww = 1;
		for( int i = 0; i < a.getFormat().getNumChannels(); i++ )
		{			
			System.out.println( "Getting channel polygon..." );
			Polygon p = aap.getChannelPolygon( i, true, (int)(w/ww) );
			
			System.out.println( "Drawing polygon for channel "+i+"..." );
			p.scaleXY( ww, -(float)ampScalar );
			p.translate( 0f, -(float)p.minY() + channelSize*i );

			System.out.println( "Polygon has "+p.nVertices()+" vertices" );
			System.out.println( "Bounding box: "+p.minX()+","+p.minY()+" "
					+p.getWidth()+"x"+p.getHeight() );
			
			renderer.drawPolygonFilled( p, new Float[]{1f,1f,1f,1f} );
			//drawLinesToPolygon( p, m, new Float[]{1f,1f,1f,1f}, 
			//		channelSize*i+channelSize/2, (int)ww );
		}
		
		m.processMaskedInline( m.flattenMax(), new PixelProcessor<Float[]>()
		{
			@Override
			public Float[] processPixel( Float[] pixel, Number[]... otherpixels )
			{
				return new Float[]{1f,0f,0f,0f};
			}
		} );
		
		try
		{
			ImageUtilities.write( m, "png", new File("audioWaveform.png") );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
		
		// Display the image
		DisplayUtilities.display( m );
    }
}
