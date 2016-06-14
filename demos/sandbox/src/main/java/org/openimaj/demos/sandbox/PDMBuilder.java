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
package org.openimaj.demos.sandbox;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.DisplayUtilities.ImageComponent;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.pixel.Pixel;

/**
 * Sample UI for building a PDM
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class PDMBuilder implements ActionListener, ChangeListener {
	private String[] pointLabels;
	private List<FImage> images;
	private ImageComponent ic;
	private Pixel[][] pointData;
	private JComboBox<String> labelsList;
	private JSpinner imageSpinner;

	public PDMBuilder(String[] pointLabels, List<FImage> images) {
		this.pointLabels = pointLabels;
		this.images = images;

		this.pointData = new Pixel[images.size()][pointLabels.length];

		createUI();
	}

	private void createUI() {
		final JFrame frame = new JFrame("PDM Builder");

		final JPanel panel = new JPanel();
		frame.getContentPane().add(panel);

		ic = new DisplayUtilities.ImageComponent(true, false);
		ic.setAllowPanning(false);
		ic.setAllowZoom(false);
		ic.setImage(ImageUtilities.createBufferedImage(images.get(0)));
		ic.setPreferredSize(ic.getSize());
		ic.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				registerClick(e.getX(), e.getY());
			}
		});
		panel.add(ic);

		final SpinnerModel model = new SpinnerNumberModel(0, 0, images.size(), 1);
		imageSpinner = new JSpinner(model);
		new JSpinner.NumberEditor(imageSpinner);
		imageSpinner.addChangeListener(this);
		panel.add(imageSpinner);

		labelsList = new JComboBox<String>(pointLabels);
		labelsList.addActionListener(this);
		panel.add(labelsList);

		frame.pack();
		frame.setVisible(true);
	}

	private void registerClick(int x, int y) {
		final int imageId = (int) imageSpinner.getModel().getValue();
		final int labelId = labelsList.getSelectedIndex();

		pointData[imageId][labelId] = new Pixel(x, y);

		int nextLabel = -1;
		int nextImage = nextImage(labelId);
		if (nextLabel == -1) {
			for (int i = 0; i < pointLabels.length; i++) {
				nextImage = nextImage(i);
				if (nextImage != -1) {
					nextLabel = i;
					break;
				}
			}
		}
		if (nextLabel == -1)
			nextLabel = labelId;
		if (nextImage == -1)
			nextImage = imageId;

		imageSpinner.setValue(nextImage);
		labelsList.setSelectedIndex(nextLabel);

		updateImage();
	}

	private int nextImage(int labelId) {
		for (int i = 0; i < images.size(); i++) {
			if (pointData[i][labelId] == null) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		updateImage();
	}

	private void updateImage() {
		final int imageId = (int) imageSpinner.getModel().getValue();

		final MBFImage img = images.get(imageId).toRGB();
		for (final Pixel p : pointData[imageId]) {
			if (p != null) {
				img.drawPoint(p, RGBColour.RED, 9);
			}
		}
		ic.setImage(ImageUtilities.createBufferedImage(img));
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		updateImage();
	}

	public static void main(String[] args) throws Exception {
		final String[] pointLabels = { "Waist", "Neck", "Head", "Left hand", "Left elbow", "Left knee", "Left foot",
				"Right foot", "Right knee", "Right elbow", "Right hand" };
		final List<FImage> images = PDMTest.loadImages();
		System.out.println("images loaded");

		new PDMBuilder(pointLabels, images);
	}
}
