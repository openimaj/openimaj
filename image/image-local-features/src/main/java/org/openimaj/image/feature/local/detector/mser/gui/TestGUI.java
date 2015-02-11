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
package org.openimaj.image.feature.local.detector.mser.gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.analysis.watershed.Component;
import org.openimaj.image.analysis.watershed.MergeTreeBuilder;
import org.openimaj.image.analysis.watershed.feature.PixelsFeature;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.feature.local.detector.mser.MSERFeatureGenerator;
import org.openimaj.image.feature.local.detector.mser.MSERFeatureGenerator.MSERDirection;
import org.openimaj.image.feature.local.detector.mser.gui.ImageUtils.ImagePanel;

/**
 * Test GUI for the MSER extractor
 *
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *
 *
 */
public class TestGUI extends JFrame
{
	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;

	private JMenuBar jJMenuBar = null;

	private JMenu jFileMenu = null;

	private JMenuItem jOpenMenuItem = null;

	private ImagePanel jImagePanel = null;

	private JPanel jControlsPanel = null;

	private List<MergeTreeBuilder> mergeTrees = null;

	protected MBFImage img;

	private JLabel jDeltaLabel = null;

	private JSlider jDeltaSlider = null;

	private JLabel jMaxAreaLabel = null;

	private JSlider jMaxAreaSlider = null;

	private JLabel jMinAreaLabel = null;

	private JSlider jMinAreaSlider1 = null;

	private JLabel jMaxVariationLabel = null;

	private JSlider jMaxVariationSlider1 = null;

	private JLabel jMinDiversityLabel = null;

	private JSlider jMinDiversitySlider = null;

	/**
	 * This is the default constructor
	 */
	public TestGUI()
	{
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 */
	private void initialize()
	{
		this.setSize(768, 541);
		this.setJMenuBar(getJJMenuBar());
		this.setContentPane(getJContentPane());
		this.setTitle("MSER Test Harness");

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e)
			{
				super.windowClosing(e);
				System.exit(1);
			}
		});
	}

	/**
	 * This method initializes jContentPane
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane()
	{
		if (jContentPane == null)
		{
			final GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 1;
			gridBagConstraints1.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints1.insets = new Insets(4, 4, 4, 4);
			gridBagConstraints1.weighty = 1.0D;
			gridBagConstraints1.gridy = 0;
			final GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.weightx = 1.0D;
			gridBagConstraints.weighty = 1.0D;
			gridBagConstraints.fill = GridBagConstraints.BOTH;
			gridBagConstraints.gridy = 0;
			jImagePanel = new ImagePanel();
			jContentPane = new JPanel();
			jContentPane.setLayout(new GridBagLayout());
			jContentPane.add(jImagePanel, gridBagConstraints);
			jContentPane.add(getJControlsPanel(), gridBagConstraints1);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jJMenuBar
	 *
	 * @return javax.swing.JMenuBar
	 */
	private JMenuBar getJJMenuBar()
	{
		if (jJMenuBar == null)
		{
			jJMenuBar = new JMenuBar();
			jJMenuBar.add(getJFileMenu());
		}
		return jJMenuBar;
	}

	/**
	 * This method initializes jFileMenu
	 *
	 * @return javax.swing.JMenu
	 */
	private JMenu getJFileMenu()
	{
		if (jFileMenu == null)
		{
			jFileMenu = new JMenu();
			jFileMenu.setText("File");
			jFileMenu.add(getJOpenMenuItem());
		}
		return jFileMenu;
	}

	/**
	 * This method initializes jOpenMenuItem
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJOpenMenuItem()
	{
		if (jOpenMenuItem == null)
		{
			jOpenMenuItem = new JMenuItem();
			jOpenMenuItem.setText("Open");
			jOpenMenuItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e)
				{
					final JFileChooser jfc = new JFileChooser();
					final int retValue = jfc.showOpenDialog(TestGUI.this);
					if (retValue == JFileChooser.APPROVE_OPTION)
					{
						final File f = jfc.getSelectedFile();
						try {
							loadFile(f);
						} catch (final IOException e1) {
							e1.printStackTrace();
						}
					}
				}
			});
		}
		return jOpenMenuItem;
	}

	private void loadFile(File f) throws IOException
	{
		// Load the image
		System.out.println("Analysing " + f);
		TestGUI.this.img = ImageUtilities.readMBF(f);
		System.out.println("Image Dimensions: " + TestGUI.this.img.getWidth() + "x" + TestGUI.this.img.getHeight());

		final long start = System.currentTimeMillis();

		final MSERFeatureGenerator mser = new MSERFeatureGenerator(1, 1, 1, 0f, 0.7f, PixelsFeature.class);

		TestGUI.this.mergeTrees = mser.performWatershed(Transforms.calculateIntensityNTSC(img));
		final long end = System.currentTimeMillis();

		// Show some stats
		final long timeTaken = end - start;
		System.out.println("--------------------------------------------");
		System.out.println("Time taken: " + timeTaken + " milliseconds");
		System.out.println("Number of pixels: " + img.getWidth() * img.getHeight());
		System.out.println("Pixels per second: " + (img.getWidth() * img.getHeight()) / (timeTaken / (double) 1000));
		System.out.println("--------------------------------------------");

		updateMSER();
	}

	/**
	 *
	 */
	private void updateMSER()
	{
		System.out.println("Delta: " + jDeltaSlider.getValue());
		System.out.println("Max Area: " + jMaxAreaSlider.getValue());
		System.out.println("Min Area: " + jMinAreaSlider1.getValue());
		System.out.println("Max Variation: " + jMaxVariationSlider1.getValue() / 100f);
		System.out.println("Min Diversity: " + jMinDiversitySlider.getValue() / 100f);

		final long start = System.currentTimeMillis();
		final MSERFeatureGenerator mser = new MSERFeatureGenerator(
				jDeltaSlider.getValue(),
				jMaxAreaSlider.getValue(),
				jMinAreaSlider1.getValue(),
				jMaxVariationSlider1.getValue() / 100f,
				jMinDiversitySlider.getValue() / 100f,
				PixelsFeature.class);
		final List<Component> up_regions = mser.performMSERDetection(this.mergeTrees, MSERDirection.Up);
		final List<Component> down_regions = mser.performMSERDetection(this.mergeTrees, MSERDirection.Down);
		final long end = System.currentTimeMillis();

		// Show some stats
		final long timeTaken = end - start;
		System.out.println("--------------------------------------------");
		System.out.println("Time taken: " + timeTaken + " milliseconds");
		System.out.println("Detected " + (up_regions.size() + down_regions.size()) + " regions");
		System.out.println("--------------------------------------------");

		BufferedImage bimg = ComponentUtils.plotComponentList(up_regions,
				ImageUtils.copyImage(ImageUtilities.createBufferedImage(img)), Color.yellow);
		bimg = ComponentUtils.plotComponentList(down_regions, bimg, Color.blue);
		jImagePanel.setImage(bimg);
	}

	/**
	 * This method initializes jControlsPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJControlsPanel()
	{
		if (jControlsPanel == null)
		{
			final GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints11.gridy = 9;
			gridBagConstraints11.weightx = 1.0;
			gridBagConstraints11.gridx = 0;
			final GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			gridBagConstraints10.gridx = 0;
			gridBagConstraints10.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints10.gridy = 8;
			jMinDiversityLabel = new JLabel();
			jMinDiversityLabel.setText("Minimum Diversity");
			final GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			gridBagConstraints9.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints9.gridy = 7;
			gridBagConstraints9.weightx = 1.0;
			gridBagConstraints9.gridx = 0;
			final GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.gridx = 0;
			gridBagConstraints8.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints8.gridy = 6;
			jMaxVariationLabel = new JLabel();
			jMaxVariationLabel.setText("Maximum Variation");
			final GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints7.gridy = 5;
			gridBagConstraints7.weightx = 1.0;
			gridBagConstraints7.gridx = 0;
			final GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.gridx = 0;
			gridBagConstraints6.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints6.gridy = 4;
			jMinAreaLabel = new JLabel();
			jMinAreaLabel.setText("Minimum Area");
			final GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints5.gridy = 3;
			gridBagConstraints5.weightx = 1.0;
			gridBagConstraints5.gridx = 0;
			final GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.gridx = 0;
			gridBagConstraints4.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints4.gridy = 2;
			jMaxAreaLabel = new JLabel();
			jMaxAreaLabel.setText("Maximum Area");
			final GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 0;
			gridBagConstraints3.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints3.gridy = 0;
			final GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints2.gridy = 1;
			gridBagConstraints2.weightx = 1.0;
			gridBagConstraints2.gridx = 0;
			jDeltaLabel = new JLabel();
			jDeltaLabel.setText("Delta");
			jControlsPanel = new JPanel();
			jControlsPanel.setLayout(new GridBagLayout());
			jControlsPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
			jControlsPanel.add(jDeltaLabel, gridBagConstraints3);
			jControlsPanel.add(getJDeltaSlider(), gridBagConstraints2);
			jControlsPanel.add(jMaxAreaLabel, gridBagConstraints4);
			jControlsPanel.add(getJMaxAreaSlider(), gridBagConstraints5);
			jControlsPanel.add(jMinAreaLabel, gridBagConstraints6);
			jControlsPanel.add(getJMinAreaSlider1(), gridBagConstraints7);
			jControlsPanel.add(jMaxVariationLabel, gridBagConstraints8);
			jControlsPanel.add(getJMaxVariationSlider1(), gridBagConstraints9);
			jControlsPanel.add(jMinDiversityLabel, gridBagConstraints10);
			jControlsPanel.add(getJMinDiversitySlider(), gridBagConstraints11);
		}
		return jControlsPanel;
	}

	/**
	 * This method initializes jDeltaSlider
	 *
	 * @return javax.swing.JSlider
	 */
	private JSlider getJDeltaSlider()
	{
		if (jDeltaSlider == null)
		{
			jDeltaSlider = new JSlider();
			jDeltaSlider.setMaximum(255);
			jDeltaSlider.setValue(10);
			jDeltaSlider.setMajorTickSpacing(50);
			jDeltaSlider.setPaintTicks(true);
			jDeltaSlider.setPaintLabels(true);
			jDeltaSlider.setMinorTickSpacing(10);
			jDeltaSlider.setMinimum(0);
			jDeltaSlider.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e)
				{
					if (!jDeltaSlider.getValueIsAdjusting())
						updateMSER();
				}
			});
		}
		return jDeltaSlider;
	}

	/**
	 * This method initializes jMaxAreaSlider
	 *
	 * @return javax.swing.JSlider
	 */
	private JSlider getJMaxAreaSlider()
	{
		if (jMaxAreaSlider == null)
		{
			jMaxAreaSlider = new JSlider();
			jMaxAreaSlider.setPaintTicks(true);
			jMaxAreaSlider.setMaximum(100000);
			jMaxAreaSlider.setMajorTickSpacing(50000);
			jMaxAreaSlider.setMinorTickSpacing(5000);
			jMaxAreaSlider.setValue(10000);
			jMaxAreaSlider.setPaintLabels(true);
			jMaxAreaSlider.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e)
				{
					if (!jMaxAreaSlider.getValueIsAdjusting())
						updateMSER();
				}
			});
		}
		return jMaxAreaSlider;
	}

	/**
	 * This method initializes jMinAreaSlider1
	 *
	 * @return javax.swing.JSlider
	 */
	private JSlider getJMinAreaSlider1()
	{
		if (jMinAreaSlider1 == null)
		{
			jMinAreaSlider1 = new JSlider();
			jMinAreaSlider1.setMajorTickSpacing(200);
			jMinAreaSlider1.setPaintTicks(true);
			jMinAreaSlider1.setMaximum(1000);
			jMinAreaSlider1.setValue(1);
			jMinAreaSlider1.setPaintLabels(true);
			jMinAreaSlider1.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e)
				{
					if (!jMinAreaSlider1.getValueIsAdjusting())
						updateMSER();
				}
			});
		}
		return jMinAreaSlider1;
	}

	/**
	 * This method initializes jMaxVariationSlider1
	 *
	 * @return javax.swing.JSlider
	 */
	private JSlider getJMaxVariationSlider1()
	{
		if (jMaxVariationSlider1 == null)
		{
			jMaxVariationSlider1 = new JSlider();
			jMaxVariationSlider1.setMaximum(1000);
			jMaxVariationSlider1.setPaintLabels(true);
			jMaxVariationSlider1.setPaintTicks(true);
			jMaxVariationSlider1.setValue(1000);
			jMaxVariationSlider1.setMajorTickSpacing(200);
			jMaxVariationSlider1.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e)
				{
					if (!jMaxVariationSlider1.getValueIsAdjusting())
						updateMSER();
				}
			});
		}
		return jMaxVariationSlider1;
	}

	/**
	 * This method initializes jMinDiversitySlider
	 *
	 * @return javax.swing.JSlider
	 */
	private JSlider getJMinDiversitySlider()
	{
		if (jMinDiversitySlider == null)
		{
			jMinDiversitySlider = new JSlider();
			jMinDiversitySlider.setMajorTickSpacing(20);
			jMinDiversitySlider.setPaintTicks(true);
			jMinDiversitySlider.setEnabled(true);
			jMinDiversitySlider.setPaintLabels(true);
			jMinDiversitySlider.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e)
				{
					if (!jMinDiversitySlider.getValueIsAdjusting())
						updateMSER();
				}
			});
		}
		return jMinDiversitySlider;
	}

	/**
	 *
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException
	{
		final TestGUI t = new TestGUI();
		t.setVisible(true);
		t.loadFile(new File("test-images/spotty-cat.jpg"));
		// t.loadFile( new File("test-images/grey-patch.png") );
	}
} // @jve:decl-index=0:visual-constraint="10,10"
