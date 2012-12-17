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
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.DefaultXYDataset;
import org.openimaj.audio.filters.MelFilterBank;
import org.openimaj.audio.filters.TriangularFilter;

/**
 * Displays Mel Filters from a Mel Filter filter bank
 * 
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 26 Jul 2012
 * @version $Author$, $Revision$, $Date$
 */
public class MelFilters extends JPanel
{
	/** */
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor
	 */
	public MelFilters()
	{
		init();
	}

	/**
	 * Initialise the chart
	 */
	private void init()
	{
		final MelFilterBank mfb = new MelFilterBank(40, 300, 3000);
		final List<TriangularFilter> filters = mfb.getFilters();

		System.out.println(filters.size() + " filters");

		DefaultXYDataset xy = new DefaultXYDataset();
		int i = 0;
		for (final TriangularFilter f : filters)
		{
			final double[][] data = new double[2][3];
			data[1][0] = f.getLowFrequency();
			data[0][0] = 0;
			data[1][1] = f.getCentreFrequency();
			data[0][1] = f.getFilterAmplitude();
			data[1][2] = f.getHighFrequency();
			data[0][2] = 0;
			xy.addSeries("Filter " + i, data);
			i++;
		}

		System.out.println(i);

		JFreeChart c = ChartFactory.createXYLineChart("Mel Filter Responses", "Amplitude",
				"Frequency", xy, PlotOrientation.HORIZONTAL, false, false,
				false);
		ChartPanel chartPanel = new ChartPanel(c, false);
		chartPanel.setPreferredSize(new Dimension(1500, 300));

		this.setLayout(new GridBagLayout());
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = gbc.gridy = 0;
		gbc.weightx = gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		this.add(chartPanel, gbc);

		xy = new DefaultXYDataset();
		i = 0;
		final int nSamples = 1024;
		for (final TriangularFilter f : filters)
		{
			final double[] response = f.getResponseCurve(nSamples, 4000);
			System.out.println("Filter " + i + ": " + Arrays.toString(response));
			final double[][] xxyy = new double[2][response.length];
			for (int x = 0; x < response.length; x++)
			{
				xxyy[1][x] = x * (4000 / nSamples);
				xxyy[0][x] = response[x];
			}
			xy.addSeries("Filter " + i, xxyy);
			i++;
		}

		c = ChartFactory.createXYLineChart("Mel Filter Calculated Responses", "Amplitude",
				"Frequency", xy, PlotOrientation.HORIZONTAL, false, false,
				false);
		chartPanel = new ChartPanel(c, false);
		chartPanel.setPreferredSize(new Dimension(1500, 300));
		gbc.gridy++;
		this.add(chartPanel, gbc);
	}

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		final JFrame f = new JFrame();
		f.getContentPane().add(new MelFilters(), BorderLayout.CENTER);
		f.pack();
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
