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
package org.openimaj.demos.image;

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openimaj.demos.Demo;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;

/**
 * Radial distortion demo
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
@Demo(author = "Sina Samangooei", description = "Demonstrates the radial distortion correction image "
		+ "processor and allows the parameters to be changed interactively "
		+ "and see the results.", keywords = { "image", "warp", "radial",
		"distortion" }, title = "Radial Distortion Calibrator", icon = "/org/openimaj/demos/icons/image/radial-icon.png")
public class RadialDistortionCalibrator {
	private static final int SLIDER_MAX = 1000;
	private MBFImage outImage;
	private MBFImage image;
	private int midX;
	private int midY;
	private float alphaY, betaY;
	private float alphaX, betaX;
	private JFrame outFrame;
	private int origMidX;
	private int origMidY;

	/**
	 * Construct the demo with the given image
	 * @param image the image
	 */
	public RadialDistortionCalibrator(MBFImage image) {
		int padding = 200;
		this.outImage = image.newInstance(image.getWidth() + padding, image.getHeight() + padding);
		this.image = image;
		this.midX = outImage.getWidth() / 2;
		this.midY = outImage.getHeight() / 2;
		this.origMidX = image.getWidth() / 2;
		this.origMidY = image.getHeight() / 2;
		this.alphaX = 0.02f;
		this.alphaY = 0.04f;
		this.betaX = 0.02f;
		this.betaY = 0.04f;
		regenAndDisplay();
		createControlWindow();
	}

	private void createControlWindow() {
		JFrame control = new JFrame();
		control.setBounds(this.outFrame.getWidth(), 0, 700, 400);
		Container cpane = control.getContentPane();
		cpane.setLayout(new GridLayout(4, 1));
		Container alphaXSlider = createSlider(new AlphaXChanger());
		Container alphaYSlider = createSlider(new AlphaYChanger());
		Container betaXSlider = createSlider(new BetaXChanger());
		Container betaYSlider = createSlider(new BetaYChanger());
		cpane.add(alphaXSlider);
		cpane.add(betaXSlider);
		cpane.add(alphaYSlider);
		cpane.add(betaYSlider);

		control.setVisible(true);
	}

	abstract class Changer implements ChangeListener, ActionListener {
		JTextField text = null;
		JSlider slider = null;

		public abstract String getName();

		public abstract boolean setNewValue(float value);

		public float min() {
			return -1f;
		}

		public float max() {
			return 1f;
		}

		public float range() {
			return max() - min();
		}

		public abstract float def();

		@Override
		public void stateChanged(ChangeEvent e) {
			JSlider source = (JSlider) e.getSource();
			if (!source.getValueIsAdjusting()) {
				int val = (int) source.getValue();
				float prop = (float) val / (float) SLIDER_MAX;
				float toSet = min() + range() * prop;
				if (setNewValue(toSet)) {
					text.setText(toSet + "");
				}
			}
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			JTextField field = (JTextField) e.getSource();
			float toSet = Float.parseFloat(field.getText());
			if (setNewValue(toSet)) {
				slider.setValue((int) (SLIDER_MAX * (toSet - min()) / range()));
			}
		}

	}

	class AlphaXChanger extends Changer {
		@Override
		public String getName() {
			return "alpha X";
		}

		@Override
		public float def() {
			return alphaX;
		}

		@Override
		public boolean setNewValue(float value) {
			boolean change = value != alphaX;
			if (change) {
				alphaX = value;
				regenAndDisplay();
			}
			return change;
		}

	}

	class AlphaYChanger extends Changer {
		@Override
		public String getName() {
			return "alpha Y";
		}

		@Override
		public float def() {
			return alphaY;
		}

		@Override
		public boolean setNewValue(float value) {
			boolean change = value != alphaY;
			if (change) {
				alphaY = value;
				regenAndDisplay();
			}
			return change;
		}
	}

	class BetaXChanger extends Changer {
		@Override
		public String getName() {
			return "beta X";
		}

		@Override
		public float def() {
			return betaX;
		}

		@Override
		public boolean setNewValue(float value) {
			boolean change = value != betaX;
			if (change) {
				betaX = value;
				regenAndDisplay();
			}
			return change;
		}

	}

	class BetaYChanger extends Changer {
		@Override
		public String getName() {
			return "beta Y";
		}

		@Override
		public float def() {
			return betaY;
		}

		@Override
		public boolean setNewValue(float value) {
			boolean change = value != betaY;
			if (change) {
				betaY = value;
				regenAndDisplay();
			}
			return change;
		}
	}

	private Container createSlider(Changer changer) {
		JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, SLIDER_MAX,
				(int) (SLIDER_MAX * ((changer.def() - changer.min()) / changer
						.range())));
		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		labelTable.put(new Integer(0), new JLabel("" + changer.min()));
		labelTable.put(new Integer(SLIDER_MAX), new JLabel("" + changer.max()));
		for (float i = 1; i < 10f; i++) {
			float prop = (i / 10f);
			String s = String.format("%.2f", (changer.min() + changer.range()
					* prop));
			labelTable.put(new Integer((int) (prop * SLIDER_MAX)),
					new JLabel(s));
		}
		slider.setLabelTable(labelTable);
		slider.setPaintLabels(true);
		slider.setBorder(BorderFactory.createTitledBorder(changer.getName()));
		slider.addChangeListener(changer);
		JPanel sliderHolder = new JPanel();
		SpringLayout layout = new SpringLayout();
		sliderHolder.setLayout(layout);
		sliderHolder.add(slider);
		JTextField text = new JTextField("" + changer.def(), 10);
		text.addActionListener(changer);
		sliderHolder.add(text);
		layout.putConstraint(SpringLayout.WEST, slider, 5, SpringLayout.WEST,
				sliderHolder);
		layout.putConstraint(SpringLayout.WEST, text, 5, SpringLayout.EAST,
				slider);
		layout.putConstraint(SpringLayout.EAST, sliderHolder, 5,
				SpringLayout.EAST, text);
		layout.putConstraint(SpringLayout.WEST, sliderHolder, 10,
				SpringLayout.WEST, slider);
		changer.slider = slider;
		changer.text = text;
		return sliderHolder;
	}

	private Point2d getDistortedPoint(Point2d point) {
		// this pixel relative to the padding
		float paddingX = point.getX();
		float paddingY = point.getY();
		// Normalise x and y such that they are in a -1 to 1 range
		float normX = (paddingX - midX) / (image.getWidth() / 2.0f);
		float normY = (paddingY - midY) / (image.getHeight() / 2.0f);

		float radius2 = normX * normX + normY * normY;
		float radius4 = radius2 * radius2;

		float xRatio = normX / (1 - alphaX * radius2 - betaX * radius4);
		float yRatio = normY / (1 - alphaY * radius2 - betaY * radius4);

		float radiusRatio2 = xRatio * xRatio + yRatio * yRatio;
		float radiusRatio4 = radiusRatio2 * radiusRatio2;

		float normDistortedX = normX
				/ (1 - alphaX * radiusRatio2 - betaX * radiusRatio4);
		float normDistortedY = normY
				/ (1 - alphaY * radiusRatio2 - betaY * radiusRatio4);

		float distortedX = ((1 + normDistortedX) / 2) * image.getWidth();
		float distortedY = ((1 + normDistortedY) / 2) * image.getHeight();
		return new Point2dImpl(distortedX, distortedY);
	}

	/**
	 * Compute the transformed point for a given point
	 * @param point the input point
	 * @return the transformed point
	 */
	public Point2d getUndistortedPoint(Point2d point) {
		// this pixel relative to the padding
		float x = point.getX();
		float y = point.getY();
		// Normalise x and y such that they are in a -1 to 1 range
		float normX = (x - origMidX) / (outImage.getWidth() / 2.0f);
		float normY = (y - origMidY) / (outImage.getHeight() / 2.0f);

		float radius2 = normX * normX + normY * normY;
		float radius4 = radius2 * radius2;

		float normundistortedX = normX - alphaX * normX * radius2 - betaX
				* normX * radius4;
		float normundistortedY = normY - alphaY * normY * radius2 - betaY
				* normY * radius4;

		float undistortedX = ((1 + normundistortedX) / 2) * outImage.getWidth();
		float undistortedY = ((1 + normundistortedY) / 2)
				* outImage.getHeight();
		return new Point2dImpl(undistortedX, undistortedY);
	}

	private void regenAndDisplay() {
		double sumDistance = 0;
		for (float y = 0; y < outImage.getHeight(); y++) {
			for (float x = 0; x < outImage.getWidth(); x++) {
				Point2dImpl point = new Point2dImpl(x, y);
				Point2d distorted = getDistortedPoint(point);

				if (image.getBounds().isInside(distorted)) {
					Point2d undistorted = getUndistortedPoint(distorted);
					sumDistance += new Line2d(point, undistorted)
							.calculateLength();
				}

				outImage.setPixel((int) x, (int) y, image.getPixelInterp(
						distorted.getX(), distorted.getY(), RGBColour.BLACK));
			}
		}
		System.out.println("Sum difference: " + sumDistance);

		if (this.outFrame == null) {
			outFrame = DisplayUtilities.display(outImage);
		} else {
			DisplayUtilities.display(outImage, outFrame);
		}
	}

	/**
	 * The main method.
	 * @param args path to an image; or empty
	 * @throws IOException
	 */
	public static void main(String args[]) throws IOException {
		if (args.length == 0)
			new RadialDistortionCalibrator(
					ImageUtilities.readMBF(RadialDistortionCalibrator.class
							.getResourceAsStream("/org/openimaj/demos/image/35smm_original.jpg")));
		else
			new RadialDistortionCalibrator(ImageUtilities.readMBF(new File(
					args[0])));
	}
}
