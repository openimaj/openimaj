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
package org.openimaj.demos.sandbox.audio;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.DefaultXYDataset;
import org.openimaj.audio.AudioPlayer;
import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.generation.Synthesizer;
import org.openimaj.audio.samples.SampleBuffer;

/**
 * 	Instantiates the synthesiser and plays the sound from the synth.
 * 	Also displays the waveform of the first 3 sample chunks delivered
 * 	by the synth.
 *
 * 	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 * 	@created 2 May 2012
 */
public class SynthesizerTest
{
	/**
	 *
	 *  @param args
	 */
	public static void main( final String[] args )
	{
		final Synthesizer synth = new Synthesizer();
		synth.setFrequency( 220 );
		SampleChunk s = synth.nextSampleChunk();
		SampleBuffer b = s.getSampleBuffer();

		int n = 3;

		final DefaultXYDataset ds = new DefaultXYDataset();

		int x = 0, y = 0;
		while( n > 0 )
		{
			// Convert sample to a XY data plot
			final double[][] data = new double[2][];
			data[0] = new double[b.size()]; // x
			data[1] = new double[b.size()]; // y

			for( x = 0; x < b.size(); x++ )
			{
				data[0][x] = b.get(x);
				data[1][x] = x+y;
			}

			s = synth.nextSampleChunk();
			b = s.getSampleBuffer();
			y += x;
			n--;

			ds.addSeries( "samples "+n, data );
		}

		final JFreeChart c = ChartFactory.createXYLineChart( "Sample", "samples",
		        "amplitude", ds, PlotOrientation.HORIZONTAL, false, false,
		        false );
		final ChartPanel chartPanel = new ChartPanel( c, false );
		chartPanel.setPreferredSize( new Dimension( 640, 480 ) );

		final JFrame f = new JFrame();
		f.add( chartPanel, BorderLayout.CENTER );
		f.pack();
		f.setVisible( true );

		// Play the audio from the synth in a new thread
		final AudioPlayer ap = AudioPlayer.createAudioPlayer( synth );
		new Thread( ap ).start();

		// Wait 2 seconds
		try
		{
			Thread.sleep( 2000 );
		}
		catch( final InterruptedException e )
		{
		}

		// Set the synth to note off
		synth.noteOff();
	}
}
