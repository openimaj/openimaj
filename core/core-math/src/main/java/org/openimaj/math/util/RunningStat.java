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
package org.openimaj.math.util;

/**
 * This class is used for providing a running mean and variance of streams of
 * data. Use the {@link #push(double)} method to add data from the stream.
 * 
 * @author John Cook (http://www.johndcook.com/)
 * @author Converted to Java by David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 25 Jan 2013
 * @version $Author$, $Revision$, $Date$
 * @see "http://www.johndcook.com/standard_deviation.html"
 */
public class RunningStat
{
	private int n = 0;

	private double m_oldM, m_newM, m_oldS, m_newS;

	/**
	 * Default constructor
	 */
	public RunningStat()
	{
		this.n = 0;
	}
	
	/**
	 * 	Constructor that takes the first value
	 *	@param firstValue the first value in the stream
	 */
	public RunningStat( final double firstValue )
	{
		this.n = 0;
		this.push( firstValue );
	}

	/**
	 * Reset the running stats
	 */
	public void clear()
	{
		this.n = 0;
	}

	/**
	 * 	Push a data value from the stream into the calculation.
	 *	@param x The data value to push
	 */
	public void push( final double x )
	{
		this.n++;

		// See Knuth TAOCP vol 2, 3rd edition, page 232
		if( this.n == 1 )
		{
			this.m_oldM = this.m_newM = x;
			this.m_oldS = 0.0;
		}
		else
		{
			this.m_newM = this.m_oldM + (x - this.m_oldM) / this.n;
			this.m_newS = this.m_oldS + (x - this.m_oldM) * (x - this.m_newM);

			// set up for next iteration
			this.m_oldM = this.m_newM;
			this.m_oldS = this.m_newS;
		}
	}

	/**
	 * Returns the number of data values that have been processed.
	 * 
	 * @return the number of data values that have been processed.
	 */
	public int numDataValues()
	{
		return this.n;
	}

	/**
	 * Returns the running mean.
	 * 
	 * @return The running mean
	 */
	public double mean()
	{
		return (this.n > 0) ? this.m_newM : 0.0;
	}

	/**
	 * Returns the running variance
	 * 
	 * @return the running variance
	 */
	public double variance()
	{
		return ((this.n > 1) ? this.m_newS / (this.n - 1) : 0.0);
	}

	/**
	 * Returns the running standard deviation
	 * 
	 * @return The running standard deviation
	 */
	public double standardDeviation()
	{
		return Math.sqrt( this.variance() );
	}
};