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
package org.openimaj.audio.filters;

import org.openimaj.audio.AudioStream;
import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.analysis.FourierTransform;
import org.openimaj.audio.processor.AudioProcessor;

/**
 *	Naive band pass filter that uses the Fourier transform to filter
 *	unwanted frequencies. The high and low pass filter values will be
 *	rounded to the nearest bin in the frequency domain, so may not exactly
 *	match the specified output. If the high pass filter frequency is greater
 *	than the low pass filter frequency, then the class will output empty
 *	audio.
 *	<p>
 *	If you want to process the samples in the chain, override the
 *	{@link #processSamples(SampleChunk)} method. This method will be called
 *	even if the FFT fails and it will be called with the input samples.
 *	<p>
 *	The class makes the assumption that all channels have the same length
 *	of data.
 *	<p>
 *	Note that this class will produce inconsistencies at the frame
 *	boundaries, so cannot sensibly be used for filtering audio that is to 
 *	be replayed.
 *	<p>
 *	Also check out the {@link EQFilter} which contains high and low pass filter
 *	implementations which can also provide band-pass filter functionality.
 *
 *	@see EQFilter
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 11 Jul 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class FFTBandPassFilter extends AudioProcessor
{
	/** The Fourier transformer */
	private FourierTransform ft = null;
	
	/** Hanning window audio processor */
	private HanningAudioProcessor hap = null;
	
	/** The lowest frequency at which audio will pass */
	private int highPassHz = 500;
	
	/** The highest frequency at which audio will pass */
	private int lowPassHz = 5000;
	
	/**
	 * 	Chainable constructor
	 * 
	 *	@param as The audio stream to process
	 * 	@param highPassHz The frequency of the high pass filter. 
	 * 	@param lowPassHz The frequency of the low pass filter.
	 */
	public FFTBandPassFilter( final AudioStream as, final int highPassHz, final int lowPassHz )
	{
		super( as );
		this.ft = new FourierTransform();
		this.hap = new HanningAudioProcessor( 1024 );
		this.highPassHz = highPassHz;
		this.lowPassHz = lowPassHz;
	}
	
	/**
	 * 	Default constructor
	 * 
	 * 	@param highPassHz The frequency of the high pass filter
	 * 	@param lowPassHz  The frequency of the low pass filter.
	 */
	public FFTBandPassFilter( final int highPassHz, final int lowPassHz )
	{
		this.ft = new FourierTransform();
		this.hap = new HanningAudioProcessor( 1024 );
		this.highPassHz = highPassHz;
		this.lowPassHz = lowPassHz;
	}
	
	/** 
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.processor.AudioProcessor#process(org.openimaj.audio.SampleChunk)
	 */
	@Override
	final public SampleChunk process( final SampleChunk sample ) throws Exception
	{		
		// Perform an FFT and get the data.
		this.ft.process( this.hap.process( sample ) );
		final float[][] transformedData = this.ft.getLastFFT();
		
		// Number of channels to process
		final int nc = transformedData.length;
		
		// If the FFT failed we'll not try to process anything
		if( nc > 0 )
		{
			// The size of each bin in Hz (using the first channel as examplar)
			final double binSize = (sample.getFormat().getSampleRateKHz()*1000) 
					/ (transformedData[0].length/2);
			
			// Work out which bins we will wipe out 
			final int highPassBin = (int)Math.floor( this.highPassHz / binSize );
			final int lowPassBin  = (int)Math.ceil(  this.lowPassHz  / binSize );
						
			// Loop through the channels.
			for( int c = 0; c < nc; c++ )
			{
				// Process the samples
				for( int i = 0; i < transformedData[c].length; i++ )
					if( i < highPassBin || i > lowPassBin )
						transformedData[c][i] = 0;
			}
			
			// Do the inverse transform from frequency to time.
			final SampleChunk s = FourierTransform.inverseTransform( 
					sample.getFormat(), transformedData );
			
			return this.processSamples( s );
		}
		else	return this.processSamples( sample );
	}

	/**
	 * 	Process the band-filtered samples.
	 *	@param sc The band-filtered samples
	 * 	@return Processed samples 
	 */
	public SampleChunk processSamples( final SampleChunk sc )
	{
		return sc;
	}
}
