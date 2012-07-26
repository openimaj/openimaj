/**
 * 
 */
package org.openimaj.demos.sandbox.audio;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.DefaultXYDataset;
import org.openimaj.audio.filters.MelFilter;
import org.openimaj.audio.filters.MelFilterBank;

/**
 *	Displays Mel Filters from a Mel Filter filter bank
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 26 Jul 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class MelFilters extends JPanel
{
	/** */
	private static final long serialVersionUID = 1L;

	/**
	 *	Default constructor
	 */
	public MelFilters()
	{
		init();
	}
	
	/**
	 * 	Initialise the chart
	 */
	private void init()
	{
		MelFilterBank mfb = new MelFilterBank();
		List<MelFilter> filters = mfb.getFilters();
		
		System.out.println( filters.size()+" filters");
		
		DefaultXYDataset xy = new DefaultXYDataset();
		int i = 0;
		for( MelFilter f : filters )
		{
			double[][] data = new double[2][3];
			data[1][0] = f.getStartFrequency();
			data[0][0] = 0;
			data[1][1] = f.getCentreFrequency();
			data[0][1] = f.getFilterAmplitude();
			data[1][2] = f.getEndFrequency();
			data[0][2] = 0;
			xy.addSeries( "Filter "+i, data );
			i++;
		}
		
        JFreeChart c = ChartFactory.createXYLineChart( "Mel Filter Responses", "Amplitude",
                "Frequency", xy, PlotOrientation.HORIZONTAL, false, false,
                false );
        ChartPanel chartPanel = new ChartPanel( c, false );
        chartPanel.setPreferredSize( new Dimension( 1500, 300 ) );
        
        this.setLayout( new GridBagLayout() );
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.weightx = gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        this.add( chartPanel, gbc );
	}
	
	/**
	 * 
	 *	@param args
	 */
	public static void main( String[] args )
	{
		JFrame f = new JFrame();
		f.getContentPane().add( new MelFilters(), BorderLayout.CENTER );
		f.pack();
		f.setVisible( true );
		f.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
	}
}
