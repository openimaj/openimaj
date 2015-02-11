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
package org.openimaj.demos.campusview;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openimaj.image.MBFImage;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.capture.Device;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.capture.VideoCaptureException;

/**
 * A JPanel that displays the output of a camera
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class CaptureComponent extends JPanel {
	private static final long serialVersionUID = 1L;

	private static final List<Device> devices = VideoCapture.getVideoDevices();

	private JComboBox<Object> comboBox;
	private JPanel panel;
	private JLabel label;

	private VideoDisplay<MBFImage> display;

	private int capHeight = 320;
	private int capWidth = 240;
	private double capRate = 25;

	private int defaultWidth = 320;
	private int defaultHeight = 240;

	/**
	 * Create the panel.
	 */
	public CaptureComponent() {
		this(640, 480, 1);
	}

	/**
	 * Create the panel.
	 *
	 * @param capWidth
	 * @param capHeight
	 * @param capRate
	 */
	public CaptureComponent(int capWidth, int capHeight, double capRate)
	{
		this.capWidth = capWidth;
		this.capHeight = capHeight;
		this.capRate = capRate;

		setOpaque(false);
		setLayout(new GridBagLayout());

		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		label = new JLabel("Camera #1");
		label.setForeground(Color.WHITE);
		label.setFont(new Font("Lucida Grande", Font.PLAIN, 16));
		label.setBounds(6, 6, 124, 16);
		add(label, gbc);

		gbc.gridy++;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weighty = 0;
		comboBox = new JComboBox<Object>();
		comboBox.setBounds(6, 286, 320, 27);
		add(comboBox, gbc);

		gbc.gridy++;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 1;
		panel = new JPanel();
		panel.setOpaque(false);
		panel.setBounds(6, 34, defaultWidth, defaultHeight);
		panel.setMaximumSize(new Dimension(defaultWidth, defaultHeight));
		add(panel, gbc);

		initSrcList();
	}

	/**
	 * Set the title of the component
	 *
	 * @param title
	 */
	public void setTitle(String title) {
		label.setText(title);
	}

	/**
	 * @return the title of the component
	 */
	public String getTitle() {
		return label.getText();
	}

	private void initSrcList() {
		comboBox.addItem("None");

		for (final Device d : devices)
			comboBox.addItem(d);

		comboBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setupVideo();
			}

		});

		comboBox.setSelectedItem(0);
	}

	private void setupVideo() {
		if (comboBox.getSelectedItem().equals("None"))
			return;

		final Device dev = (Device) comboBox.getSelectedItem();

		if (display != null) {
			((VideoCapture) display.getVideo()).stopCapture();
			panel.removeAll();
		}

		System.out.println(dev);

		try {
			display = VideoDisplay.createVideoDisplay(new VideoCapture(capWidth, capHeight, capRate, dev), panel);
		} catch (final VideoCaptureException e) {
			throw new RuntimeException(e);
		}

		revalidate();
		repaint();
	}

	/**
	 * @return the capHeight
	 */
	public int getCapHeight() {
		return capHeight;
	}

	/**
	 * @param capHeight
	 *            the capHeight to set
	 */
	public void setCapHeight(int capHeight) {
		this.capHeight = capHeight;
		setupVideo();
	}

	/**
	 * @return the capWidth
	 */
	public int getCapWidth() {
		return capWidth;
	}

	/**
	 * @param capWidth
	 *            the capWidth to set
	 */
	public void setCapWidth(int capWidth) {
		this.capWidth = capWidth;
		setupVideo();
	}

	/**
	 * @return the capRate
	 */
	public double getCapRate() {
		return capRate;
	}

	@Override
	public int getWidth()
	{
		if (display != null)
			return display.getScreen().getWidth();
		return defaultWidth;
	}

	@Override
	public int getHeight()
	{
		if (display != null)
			return display.getScreen().getHeight();
		return defaultHeight;
	}

	/**
	 * @param capRate
	 *            the capRate to set
	 */
	public void setCapRate(double capRate) {
		this.capRate = capRate;
		setupVideo();
	}

	/**
	 * @return the current frame
	 */
	public MBFImage getCurrentFrame() {
		if (display != null)
			return display.getVideo().getCurrentFrame();
		return null;
	}
}
