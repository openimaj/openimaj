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
import org.openimaj.audio.processor.AudioProcessor;
import org.openimaj.audio.samples.SampleBuffer;

/**
 *	A class that encapsulates a bunch of different EQ-based filter algorithms
 *	that use a standard bi-quad filter (4th order Linkwitz-Riley filter).
 *
 *	@see "http://musicdsp.org/showArchiveComment.php?ArchiveID=266"
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 19 Jul 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class EQFilter extends AudioProcessor
{
	/**
	 * 	A class that represents the bi-quad coefficients for a filter.
	 *
	 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *  @created 20 Jul 2012
	 *	@version $Author$, $Revision$, $Date$
	 */
	public static class EQVars
	{
		double a0, a1, a2, a3, a4;
		double b0, b1, b2, b3, b4;
	}
	
	/**
	 *	An enumerator for various audio filters.
	 *
	 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *  @created 19 Jul 2012
	 *	@version $Author$, $Revision$, $Date$
	 */
	public static enum EQType
	{
		/**
		 *	Low pass filter 
		 */
		LPF
		{
			@Override
			public EQVars getCoefficients( final double frequency, final double sampleRate )
			{
				final double wc  = 2 * Math.PI * frequency;
				final double wc2 = wc * wc;
				final double wc3 = wc2 * wc;
				final double wc4 = wc2 * wc2;
				final double k = wc/Math.tan(Math.PI*frequency/sampleRate);
				final double k2 = k * k;
				final double k3 = k2 * k;
				final double k4 = k2 * k2;
				final double sqrt2 = Math.sqrt(2);
				final double sq_tmp1 = sqrt2 * wc3 * k;
				final double sq_tmp2 = sqrt2 * wc * k3;
				final double a_tmp = 4 * wc2 * k2 + 2*sq_tmp1 + k4 + 2*sq_tmp2 + wc4;

				final EQVars v = new EQVars();
				
				v.b1 = (4*(wc4+sq_tmp1-k4-sq_tmp2)) / a_tmp;
				v.b2 = (6*wc4-8*wc2*k2+6*k4)/a_tmp;
				v.b3 = (4*(wc4-sq_tmp1+sq_tmp2-k4))/a_tmp;
				v.b4 = (k4-2*sq_tmp1+wc4-2*sq_tmp2+4*wc2*k2)/a_tmp;

				//================================================
				// low-pass
				//================================================
				v.a0 = wc4/a_tmp;
				v.a1 = 4*wc4/a_tmp;
				v.a2 = 6*wc4/a_tmp;
				v.a3 = v.a1;
				v.a4 = v.a0;
				
				return v;
			}
		},
		
		/**
		 *	High pass filter 
		 */
		HPF
		{
			@Override
			public EQVars getCoefficients( final double frequency, final double sampleRate )
			{
				final double wc  = 2 * Math.PI * frequency;
				final double wc2 = wc * wc;
				final double wc3 = wc2 * wc;
				final double wc4 = wc2 * wc2;
				final double k = wc/Math.tan(Math.PI*frequency/sampleRate);
				final double k2 = k * k;
				final double k3 = k2 * k;
				final double k4 = k2 * k2;
				final double sqrt2 = Math.sqrt(2);
				final double sq_tmp1 = sqrt2 * wc3 * k;
				final double sq_tmp2 = sqrt2 * wc * k3;
				final double a_tmp = 4 * wc2 * k2 + 2*sq_tmp1 + k4 + 2*sq_tmp2 + wc4;

				final EQVars v = new EQVars();
				
				v.b1 = (4*(wc4+sq_tmp1-k4-sq_tmp2)) / a_tmp;
				v.b2 = (6*wc4-8*wc2*k2+6*k4)/a_tmp;
				v.b3 = (4*(wc4-sq_tmp1+sq_tmp2-k4))/a_tmp;
				v.b4 = (k4-2*sq_tmp1+wc4-2*sq_tmp2+4*wc2*k2)/a_tmp;

				//================================================
				// high-pass
				//================================================
				v.a0 = k4/a_tmp;
				v.a1 = -4*k4/a_tmp;
				v.a2 = 6*k4/a_tmp;
				v.a3 = v.a1;
				v.a4 = v.a0;
				
				return v;
			}
		};
		
		/**
		 * 	Initialise the filter
		 *	@param frequency The frequency of the filter
		 *	@param sampleRate The sample rate of the samples
		 * 	@return The biquad coefficients for this filter 
		 */
		public abstract EQVars getCoefficients( double frequency, double sampleRate );
	}
	
	/** The type of EQ process */
	private EQType type = null;
	
	/** The cached biquad coefficients for this filter */
	private EQVars vars = null;
	
	/** The frequency of the filter */
	private double frequency = 0;

	// These variables are used during the loop
	// and are stored between loops to avoid clicking
	private double xm1 = 0, xm2 = 0, xm3 = 0, xm4 = 0, 
				   ym1 = 0, ym2 = 0, ym3 = 0, ym4 = 0;
	
	/**
	 * 	Default constructor for ad-hoc processing.
	 *	@param type The type of EQ
	 * 	@param f The frequency of the filter 
	 */
	public EQFilter( final EQType type, final double f )
	{
		this.type = type;
		this.frequency = f;
	}
	
	/**
	 * 	Chainable constructor for stream processing.
	 *	@param as The audio stream to process
	 *	@param type The type of EQ
	 * 	@param f The frequency of the filter 
	 */
	public EQFilter( final AudioStream as, final EQType type, final double f )
	{
		super( as );
		this.type = type;
		this.frequency = f;
	}
	
	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.processor.AudioProcessor#process(org.openimaj.audio.SampleChunk)
	 */
	@Override
	public SampleChunk process( final SampleChunk sample ) throws Exception
	{
		if( this.vars == null )
			this.vars = this.type.getCoefficients( this.frequency, 
					sample.getFormat().getSampleRateKHz() *1000d );

		// Standard bi-quad processing of the sample chunk
		// We have to process each channel independently because the function
		// is recursive.
		final SampleBuffer sb = sample.getSampleBuffer();
		final int nChans = sample.getFormat().getNumChannels();
		for( int c = 0; c < nChans; c++ )
		{
			this.xm1 = this.xm2 = this.xm3 = this.xm4 = 0;
			this.ym1 = this.ym2 = this.ym3 = this.ym4 = 0;
			for( int n = c; n < sb.size(); n += nChans )
			{
				final double tempx = sb.get(n);
				final double tempy = this.vars.a0*tempx + this.vars.a1*this.xm1 
						+ this.vars.a2*this.xm2 + this.vars.a3*this.xm3 + 
						this.vars.a4*this.xm4 - this.vars.b1*this.ym1 - 
						this.vars.b2*this.ym2 - this.vars.b3*this.ym3 - 
						this.vars.b4*this.ym4;
				
				this.xm4 = this.xm3; this.xm3 = this.xm2; this.xm2 = this.xm1;
				this.xm1 = tempx;
				this.ym4 = this.ym3; this.ym3 = this.ym2; this.ym2 = this.ym1;
				this.ym1 = tempy;
		
				sb.set( n, (float)tempy );
			}
		}
			
		return sample;
	}
}
