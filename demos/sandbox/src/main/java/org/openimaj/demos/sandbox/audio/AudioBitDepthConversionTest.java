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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.DefaultXYDataset;
import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.AudioPlayer;
import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.conversion.BitDepthConverter;
import org.openimaj.audio.conversion.BitDepthConverter.BitDepthConversionAlgorithm;
import org.openimaj.audio.generation.Synthesizer;
import org.openimaj.audio.samples.SampleBuffer;


/**
 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *
 *	@created 18 Jun 2012
 */
public class AudioBitDepthConversionTest
{
	/**
	 *  @param args
	 */
	public static void main( final String[] args )
    {
		try
        {
	        // ============================================================== //
	        // This is what we'll convert from
	        final AudioFormat inputFormat  = new AudioFormat( 16, 22.05, 1 );

	        // This is what we'll convert to
	        final AudioFormat outputFormat1 = new AudioFormat( 8, 22.05, 1 );

	        // Create a synthesiser to output stuff
	        final Synthesizer synth = new Synthesizer();
	        synth.setFrequency( 500 );
	        synth.setFormat( inputFormat );
//	        XuggleAudio xa = new XuggleAudio( new File( "videoplayback.3gp" ) );
//	        MultichannelToMonoProcessor synth = new MultichannelToMonoProcessor(xa);

	        // The sample rate converter we're testing
	        final BitDepthConverter bdc1 = new BitDepthConverter( synth,
	        		BitDepthConversionAlgorithm.NEAREST,
	        		outputFormat1 );

	        // ============================================================== //
	        // Add the synth's chunks to the display
	        final ArrayList<SampleChunk> chunks = new ArrayList<SampleChunk>();
	        for( int n = 0; n < 3; n++ )
	        	chunks.add( synth.nextSampleChunk().clone() );
	        final DefaultXYDataset ds1 = AudioBitDepthConversionTest.getDataSet( chunks );

	        // ============================================================== //
	        // Now add the resampled chunks to the display
	        final ArrayList<SampleChunk> resampledChunks1 = new ArrayList<SampleChunk>();
	        for( int n = 0; n < chunks.size(); n++ )
	        	resampledChunks1.add( bdc1.process( chunks.get(n) ).clone() );
	        final DefaultXYDataset ds2 = AudioBitDepthConversionTest.getDataSet( resampledChunks1 );

	        // ============================================================== //
	        // Set up the display
	        final JPanel p = new JPanel( new GridBagLayout() );
	        final GridBagConstraints gbc = new GridBagConstraints();
	        gbc.gridx = gbc.gridy = 0;
	        gbc.fill = GridBagConstraints.BOTH;
	        gbc.weightx = gbc.weighty = 1;

	        // ============================================================== //
	        // Add a chart of the original samples
	        JFreeChart c = ChartFactory.createXYLineChart( "Samples @ "+inputFormat, "Amplitude",
	                "Samples", ds1, PlotOrientation.HORIZONTAL, false, false,
	                false );
	        ChartPanel chartPanel = new ChartPanel( c, false );
	        chartPanel.setPreferredSize( new Dimension( 1500, 300 ) );

	        gbc.gridy++;
	        p.add( chartPanel, gbc );

	        // ============================================================== //
	        // Add a chart of the resampled samples
	        c = ChartFactory.createXYLineChart( "Samples @ "+outputFormat1, "Amplitude",
	                "Samples", ds2, PlotOrientation.HORIZONTAL, false, false,
	                false );
	        chartPanel = new ChartPanel( c, false );
	        chartPanel.setPreferredSize( new Dimension( 1500, 300 ) );

	        gbc.gridy++;
	        p.add( chartPanel, gbc );

	        // ============================================================== //
	        // Display
	        final JFrame f = new JFrame();
	        f.add( p, BorderLayout.CENTER );
	        f.pack();
	        f.setVisible( true );

	        // Play the sample (minus the first three chunks ;)
	        final AudioPlayer ap = new AudioPlayer( bdc1 );
	        ap.run();

        }
        catch( final HeadlessException e )
        {
	        e.printStackTrace();
        }
        catch( final Exception e )
        {
	        e.printStackTrace();
        }
    }

	/**
	 * 	Returns a data set that displays the sample chunks.
	 *
	 *  @param chunks
	 *  @return a dataset
	 */
	public static DefaultXYDataset getDataSet( final List<SampleChunk> chunks )
	{
		final DefaultXYDataset ds = new DefaultXYDataset();

		int x = 0, y = 0;
		for( int n = 0; n < chunks.size(); n++ )
		{
			final SampleChunk sc = chunks.get(n);
			final SampleBuffer b = sc.getSampleBuffer();

			// Convert sample to a XY data plot
			final double[][] data = new double[2][];
			data[0] = new double[b.size()]; // x
			data[1] = new double[b.size()]; // y

			for( x = 0; x < b.size(); x++ )
			{
				data[0][x] = b.get(x);
				data[1][x] = x+y;
			}

			y += x;

			ds.addSeries( "samples "+n, data );
		}

		return ds;
	}
}
