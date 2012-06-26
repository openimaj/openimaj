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
import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.Synthesizer;
import org.openimaj.audio.Synthesizer.WaveType;
import org.openimaj.audio.conversion.SampleRateConverter;
import org.openimaj.audio.conversion.SampleRateConverter.SampleRateConversionAlgorithm;
import org.openimaj.audio.samples.SampleBuffer;


/**
 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	
 *	@created 18 Jun 2012
 */
public class AudioSampleRateConversionTest
{
	/**
	 *  @param args
	 */
	public static void main( String[] args )
    {
		try
        {
	        // ============================================================== //
	        // This is what we'll convert from
	        AudioFormat inputFormat  = new AudioFormat( 16, 22.05, 1 );
	        
	        // This is what we'll convert to
	        AudioFormat outputFormat1 = new AudioFormat( 16, 11.025, 1 );
	        AudioFormat outputFormat2 = new AudioFormat( 16, 44.1, 1 );
	        
	        // Create a synthesiser to output stuff
	        Synthesizer synth = new Synthesizer();
	        synth.setFrequency( 500 );
	        synth.setFormat( inputFormat );
	        synth.setOscillatorType( WaveType.SINE );
	        
	        // The sample rate converter we're testing
	        SampleRateConverter src1 = new SampleRateConverter( 
	        		SampleRateConversionAlgorithm.LINEAR_INTERPOLATION, 
	        		outputFormat1 );
	        SampleRateConverter src2 = new SampleRateConverter( 
	        		SampleRateConversionAlgorithm.LINEAR_INTERPOLATION, 
	        		outputFormat2 );
	        
	        // ============================================================== //
	        // Add the synth's chunks to the display
	        ArrayList<SampleChunk> chunks = new ArrayList<SampleChunk>();
	        for( int n = 0; n < 3; n++ )
	        	chunks.add( synth.nextSampleChunk() );
	        DefaultXYDataset ds1 = getDataSet( chunks );
	        
	        // ============================================================== //
	        // Now add the resampled chunks to the display
	        ArrayList<SampleChunk> resampledChunks1 = new ArrayList<SampleChunk>();
	        for( int n = 0; n < chunks.size(); n++ )
	        	resampledChunks1.add( src1.process( chunks.get(n) ) );
	        DefaultXYDataset ds2 = getDataSet( resampledChunks1 );	    
	        // ============================================================== //
	        ArrayList<SampleChunk> resampledChunks2 = new ArrayList<SampleChunk>();
	        for( int n = 0; n < chunks.size(); n++ )
	        	resampledChunks2.add( src2.process( chunks.get(n) ) );
	        DefaultXYDataset ds3 = getDataSet( resampledChunks2 );	    
	        
	        // ============================================================== //
	        // Set up the display
	        JPanel p = new JPanel( new GridBagLayout() );
	        GridBagConstraints gbc = new GridBagConstraints();
	        gbc.gridx = gbc.gridy = 0;
	        gbc.fill = GridBagConstraints.BOTH;
	        gbc.weightx = gbc.weighty = 1;

	        // ============================================================== //
	        // Add a chart of the original samples
	        JFreeChart c = ChartFactory.createXYLineChart( "Original Samples", "Amplitude",
	                "Samples", ds1, PlotOrientation.HORIZONTAL, false, false,
	                false );
	        ChartPanel chartPanel = new ChartPanel( c, false );
	        chartPanel.setPreferredSize( new Dimension( 1500, 300 ) );

	        gbc.gridy++;
	        p.add( chartPanel, gbc );

	        // ============================================================== //
	        // Add a chart of the resampled samples
	        c = ChartFactory.createXYLineChart( "Downsampled Samples", "Amplitude",
	                "Samples", ds2, PlotOrientation.HORIZONTAL, false, false,
	                false );
	        chartPanel = new ChartPanel( c, false );
	        chartPanel.setPreferredSize( new Dimension( 1500, 300 ) );
	        
	        gbc.gridy++;
	        p.add( chartPanel, gbc );
	        
	        // ============================================================== //
	        // Add a chart of the resampled samples
	        c = ChartFactory.createXYLineChart( "Upsampled Samples", "Amplitude",
	                "Samples", ds3, PlotOrientation.HORIZONTAL, false, false,
	                false );
	        chartPanel = new ChartPanel( c, false );
	        chartPanel.setPreferredSize( new Dimension( 1500, 300 ) );
	        
	        gbc.gridy++;
	        p.add( chartPanel, gbc );

	        // ============================================================== //
	        // Display
	        JFrame f = new JFrame();
	        f.add( p, BorderLayout.CENTER );
	        f.pack();
	        f.setVisible( true );
        }
        catch( HeadlessException e )
        {
	        e.printStackTrace();
        }
        catch( Exception e )
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
	public static DefaultXYDataset getDataSet( List<SampleChunk> chunks )
	{
		DefaultXYDataset ds = new DefaultXYDataset();
		
		int x = 0, y = 0;
		for( int n = 0; n < chunks.size(); n++ )
		{
			SampleChunk sc = chunks.get(n);
			SampleBuffer b = sc.getSampleBuffer();
			
			// Convert sample to a XY data plot
			double[][] data = new double[2][];
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
