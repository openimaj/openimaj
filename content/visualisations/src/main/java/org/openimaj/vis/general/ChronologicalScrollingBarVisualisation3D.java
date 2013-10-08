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
package org.openimaj.vis.general;

import java.awt.Dimension;

import org.openimaj.image.MBFImage;
import org.openimaj.vis.Visualisation;

/**
 *	A chronological bar chart, where each time you add a data point, the visualisation is
 *	moved along and another set of data is added to the 3D visualisation. Data must therefore
 *	be one dimensional.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 8 Jul 2013
 */
public class ChronologicalScrollingBarVisualisation3D implements Visualisation<double[]>
{
	/** The 3D bar visualisation used to display the time series */
	private final BarVisualisation3D barVis;

	/** The data */
	private final double[][] data;

	/** The expected length of each data line */
	private final int expectedDataLength;

	/**
	 *	Default constructor
	 *	@param width Width in pixels
	 *	@param height Height in pixels
	 *	@param timeLength The number of time points to show
	 * 	@param dataWidth Length of each data line
	 */
	public ChronologicalScrollingBarVisualisation3D( final int width, final int height, final int timeLength, final int dataWidth )
	{
		this.barVis = new BarVisualisation3D( width, height );
		this.data = new double[timeLength][dataWidth];
		this.expectedDataLength = dataWidth;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.VisualisationImageProvider#updateVis()
	 */
	@Override
	public void updateVis()
	{
		this.barVis.updateVis();
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.VisualisationImageProvider#getVisualisationImage()
	 */
	@Override
	public MBFImage getVisualisationImage()
	{
		return this.barVis.getVisualisationImage();
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.VisualisationImageProvider#setRequiredSize(java.awt.Dimension)
	 */
	@Override
	public void setRequiredSize( final Dimension d )
	{
		this.barVis.setRequiredSize( d );
	}

	/**
	 * 	Set the maximum data value
	 *	@param max The maximum
	 */
	public void setMaximum( final double max )
	{
		this.barVis.setMaximum( max );
	}

	/**
	 * 	Set the x axis name
	 *	@param xAxis The x axis
	 */
	public void setXAxisName( final String xAxis )
	{
		this.barVis.setxAxisName( xAxis );
	}

	/**
	 * 	Set the y axis name
	 *	@param yAxis The y axis
	 */
	public void setYAxisName( final String yAxis )
	{
		this.barVis.setyAxisName( yAxis );
	}

	/**
	 * 	Set the z axis name
	 *	@param zAxis The z axis
	 */
	public void setZAxisName( final String zAxis )
	{
		this.barVis.setzAxisName( zAxis );
	}

	/**
	 * 	Data is copied into the visualisation, so the arrays passed in are untouched.
	 *
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.Visualisation#setData(java.lang.Object)
	 */
	@Override
	public void setData( final double[] newData )
	{
		if( newData.length != this.expectedDataLength )
			throw new IllegalArgumentException( "WARNING: Data was not the correct length. Expected "
						+this.expectedDataLength+" got "+newData.length );

		// Move all the old data up
		for( int i = 1; i < this.data.length; i++ )
			System.arraycopy( this.data[i], 0, this.data[i-1], 0, this.expectedDataLength );
		System.arraycopy( newData, 0, this.data[this.data.length-1], 0, this.expectedDataLength );

		this.barVis.setData( this.data );
	}

	/**
	 * 	Test
	 *	@param args
	 * @throws InterruptedException
	 */
	public static void main( final String[] args ) throws InterruptedException
	{
		// Number of data points
		final int N = 10;

		// Number of data frames to draw
		final int F = 2500;

		// Number of time points to draw
		final int T = 50;

		// Sleep time
		final int S = 25;

		final ChronologicalScrollingBarVisualisation3D c =
				new ChronologicalScrollingBarVisualisation3D( 800, 800, T, N );
		c.setMaximum( 2 );
		c.setXAxisName( "Scrolling Bar Demo" );
		c.setZAxisName( "Time" );
		c.setYAxisName( "Value" );

		for( int i = 0; i < F; i++ )
		{
			final double[] d = new double[N];
			for( int j = 0; j < N; j++ )
				d[j] = Math.sin( (N+i+4*j)/20d ) + 1d;
			c.setData( d );
			Thread.sleep( S );
		}
	}
}
