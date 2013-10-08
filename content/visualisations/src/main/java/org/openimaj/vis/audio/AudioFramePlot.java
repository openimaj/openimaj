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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.DefaultXYDataset;
import org.openimaj.audio.AudioStream;
import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.samples.SampleBuffer;
import org.openimaj.util.pair.IndependentPair;

/**
 * 	Allows the drawing of frames of audio onto an interactive line chart (JFreeChart XY plot).
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 13 Feb 2013
 *	@version $Author$, $Revision$, $Date$
 */
public class AudioFramePlot
{
	/**
	 * 	Draws the first 3 frames of the audio stream on to a chart.  If the number of streams
	 * 	is 1, then the frames will be coloured, otherwise the streams will be coloured. The
	 * 	streams will be named "Stream n" and a legend shown if there is more than one stream.
	 *
	 *	@param streams The audio stream
	 */
	public static void drawChart( final AudioStream ... streams )
	{
		if( streams.length == 1 )
				AudioFramePlot.drawChart( 3, true, streams );
		else	AudioFramePlot.drawChart( 3, false, streams );
	}

	/**
	 * 	Draws the first n frames of the audio stream on to a chart. If the number of streams
	 * 	is 1, then the frames will be coloured, otherwise the streams will be coloured. The
	 * 	streams will be named "Stream n" and a legend shown if there is more than one stream.
	 *
	 *	@param numFrames The number of frames to draw
	 *	@param streams The audio streams
	 */
	public static void drawChart( final int numFrames, final AudioStream ... streams )
	{
		if( streams.length == 1 )
				AudioFramePlot.drawChart( numFrames, true, streams );
		else	AudioFramePlot.drawChart( numFrames, false, streams );
	}

	/**
	 * 	Draws the first n frames of the audio stream on to a chart. Will label each of the streams
	 * 	as "Stream n" and a legend will be shown on the chart if there is more than one stream.
	 *
	 *	@param numFrames The number of frames to draw
	 * 	@param colouredFrames Whether to colour individual frames
	 *	@param streams The audio streams
	 */
	public static void drawChart( final int numFrames, final boolean colouredFrames, final AudioStream ... streams )
	{
		final List<IndependentPair<AudioStream, String>> pairs =
				new ArrayList<IndependentPair<AudioStream,String>>();

		for( int i = 0; i < streams.length; i++ )
			pairs.add( new IndependentPair<AudioStream, String>( streams[i], "Stream "+i ) );

		AudioFramePlot.drawChart( numFrames, colouredFrames, pairs );
	}

	/**
	 * 	Draws the first n frames of the audio streams on to a chart mapping the names given
	 * 	to each stream into the legend. Note that the legend will only be shown if there is more
	 * 	than one stream.
	 *
	 *	@param numFrames The number of frames to draw
	 * 	@param colouredFrames Whether to colour individual frames
	 *	@param streams The audio streams and their labels
	 */
	public static void drawChart( final int numFrames, final boolean colouredFrames,
			final List<IndependentPair<AudioStream,String>> streams )
	{
		final DefaultXYDataset ds = new DefaultXYDataset();

		for( final IndependentPair<AudioStream, String> asl : streams )
		{
			final AudioStream as = asl.firstObject();
			final String label = asl.secondObject();

			SampleChunk s = as.nextSampleChunk();
			SampleBuffer b = s.getSampleBuffer();

			int x = 0;
			int y = 0;
			double[][] data = new double[2][];
			if( !colouredFrames )
			{
				data[0] = new double[b.size()*numFrames]; // x
				data[1] = new double[b.size()*numFrames]; // y
			}

			for( int n = 0; n < numFrames; n++ )
			{
				s = as.nextSampleChunk();
				if( s == null ) break;

				// Convert sample to a XY data plot
				if( colouredFrames )
				{
					data = new double[2][];
					data[0] = new double[b.size()]; // x
					data[1] = new double[b.size()]; // y
					x = 0;
				}

				System.out.println( "Loop "+x+" to "+(x+b.size())+", y = "+y);
				// Copy the value into the data series
				for( int z = x; z < x+b.size(); z++ )
				{
					data[0][z] = b.get(z-x);
					data[1][z] = z+y;
				}

				// Add as a series if we're using coloured frames
				if( colouredFrames )
				{
					y += b.size();
					ds.addSeries( label+", Frame "+n, data );
				}
				else	x += b.size();

				// Get ready for next loop
				b = s.getSampleBuffer();
			}

			if( !colouredFrames )
				ds.addSeries( label, data );
		}

		final JFreeChart chart = ChartFactory.createXYLineChart( "Sample", "samples",
		        "amplitude", ds, PlotOrientation.HORIZONTAL, streams.size() > 1, false,
		        false );
		final ChartPanel chartPanel = new ChartPanel( chart, false );
		chartPanel.setPreferredSize( new Dimension( 1280, 480 ) );

		final JFrame f = new JFrame();
		f.add( chartPanel, BorderLayout.CENTER );
		f.pack();
		f.setVisible( true );
	}
}

