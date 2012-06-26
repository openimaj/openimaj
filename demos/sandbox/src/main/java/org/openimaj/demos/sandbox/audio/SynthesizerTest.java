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
import org.openimaj.audio.Synthesizer;
import org.openimaj.audio.Synthesizer.WaveType;
import org.openimaj.audio.samples.SampleBuffer;

/**
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * 
 * @created 2 May 2012
 */
public class SynthesizerTest
{
	/**
	 * 
	 *  @param args
	 */
	public static void main( String[] args )
	{
		Synthesizer synth = new Synthesizer();
		synth.setFrequency( 440 );
		synth.setOscillatorType( WaveType.SINE );
		SampleChunk s = synth.nextSampleChunk();
		SampleBuffer b = s.getSampleBuffer();
		
		int n = 3;

		DefaultXYDataset ds = new DefaultXYDataset();
		
		int x = 0, y = 0;		
		while( n > 0 )
		{
			// Convert sample to a XY data plot
			double[][] data = new double[2][];
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

		JFreeChart c = ChartFactory.createXYLineChart( "Sample", "samples",
		        "amplitude", ds, PlotOrientation.HORIZONTAL, false, false,
		        false );
		ChartPanel chartPanel = new ChartPanel( c, false );
		chartPanel.setPreferredSize( new Dimension( 640, 480 ) );

		JFrame f = new JFrame();
		f.add( chartPanel, BorderLayout.CENTER );
		f.pack();
		f.setVisible( true );
		
		AudioPlayer ap = AudioPlayer.createAudioPlayer( synth );
		ap.run();
	}
}
