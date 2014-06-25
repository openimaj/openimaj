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

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.event.MouseInputListener;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.connectedcomponent.ConnectedComponentLabeler;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.pixel.PixelSet;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Circle;
import org.openimaj.math.geometry.transforms.HomographyModel;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.util.pair.Pair;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;

public class DigitalWhiteboard implements VideoDisplayListener<MBFImage>, MouseInputListener, KeyListener {
	private VideoCapture capture;
	private VideoDisplay<MBFImage> display;
	private JFrame drawingFrame;
	private MBFImage drawingPanel;
	private Runnable drawingUpdater;
	private ConnectedComponentLabeler labeler;
	List<MBFImage> learningFrames = new ArrayList<MBFImage>();
	// private HistogramPixelModel model = null;
	private MODE mode = MODE.NONE;
	private HomographyModel homography = null;
	private List<Pair<Point2d>> homographyPoints = new ArrayList<Pair<Point2d>>();
	private List<IndependentPair<String, Point2d>> calibrationPoints = new ArrayList<IndependentPair<String, Point2d>>();
	private int calibrationPointIndex;
	private Point2dImpl previousPoint;
	private double screenDiagonal;

	enum MODE {
		MODEL, SEARCHING, NONE, LINE_CONSTRUCTING;
	}

	public DigitalWhiteboard() throws IOException {

		System.out.println(VideoCapture.getVideoDevices());
		capture = new VideoCapture(320, 240, VideoCapture.getVideoDevices().get(1));
		final JFrame screen = DisplayUtilities.makeFrame("Video");
		display = VideoDisplay.createVideoDisplay(capture, screen);
		display.addVideoListener(this);
		display.displayMode(true);
		display.getScreen().addKeyListener(this);
		// GraphicsDevice device =
		// GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		final GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[1];
		drawingPanel = new MBFImage(device.getDisplayMode().getWidth(), device.getDisplayMode().getHeight(),
				ColourSpace.RGB);
		// drawingPanel = new MBFImage(640,480,ColourSpace.RGB);
		drawingPanel.fill(RGBColour.WHITE);
		drawingFrame = DisplayUtilities.display(drawingPanel);
		drawingFrame.setBounds(640, 0, drawingPanel.getWidth(), drawingPanel.getHeight());
		drawingFrame.addKeyListener(this);
		// drawingFrame.setUndecorated(true);
		drawingFrame.setIgnoreRepaint(true);
		drawingFrame.setResizable(false);
		// drawingFrame.setVisible(false);
		// drawingFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		// device.setFullScreenWindow(drawingFrame);
		// GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0].setFullScreenWindow(screen);

		// screenDiagonal =
		// Math.sqrt(Math.pow(display.getScreen().getWidth()/2,2) +
		// Math.pow(display.getScreen().getHeight()/2,2));
		screenDiagonal = 50;

		drawingUpdater = new Runnable() {

			@Override
			public void run() {
				while (true) {
					// drawingPanel = new MBFImage(640,480,ColourSpace.RGB);
					try {
						drawWhiteboard(drawingPanel);
						DisplayUtilities.display(drawingPanel, drawingFrame);

						Thread.sleep(1000 / 30);
					} catch (final InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		};
		final Thread t = new Thread(drawingUpdater);
		t.start();

		labeler = new ConnectedComponentLabeler(ConnectedComponent.ConnectMode.CONNECT_4);

		calibrationPoints.add(new IndependentPair<String, Point2d>("TOP LEFT", new Point2dImpl(20, 20)));
		calibrationPoints.add(new IndependentPair<String, Point2d>("TOP RIGHT", new Point2dImpl(
				drawingPanel.getWidth() - 20, 20)));
		calibrationPoints.add(new IndependentPair<String, Point2d>("BOTTOM LEFT", new Point2dImpl(20, drawingPanel
				.getHeight() - 20)));
		calibrationPoints.add(new IndependentPair<String, Point2d>("BOTTOM RIGHT", new Point2dImpl(drawingPanel
				.getWidth() - 20, drawingPanel.getHeight() - 20)));
		calibrationPointIndex = 0;
	}

	private synchronized void drawWhiteboard(MBFImage drawingPanel) {
		// drawingPanel.fill(RGBColour.WHITE);
		if (mode == MODE.MODEL || this.calibrationPointIndex < this.calibrationPoints.size()) {
			drawingPanel.fill(RGBColour.WHITE);
			final Point2d waitingForPoint = this.calibrationPoints.get(calibrationPointIndex).secondObject();
			drawingPanel.createRenderer().drawShape(new Circle(waitingForPoint.getX(), waitingForPoint.getY(), 10),
					RGBColour.RED);
		}

	}

	@Override
	public void afterUpdate(VideoDisplay<MBFImage> display) {
		// TODO Auto-generated method stub

	}

	@Override
	public synchronized void beforeUpdate(MBFImage frame) {

		final FImage greyFramePart = Transforms.calculateIntensityNTSC(frame);
		greyFramePart.threshold(0.99f);
		// greyFramePart.processInplace(new AdaptiveLocalThresholdMean(5));
		// greyFramePart.processInplace(new OtsuThreshold());
		// FImage greyFramePart = frame.getBand(0);
		final MBFImage greyFrame = new MBFImage(new FImage[] { greyFramePart.clone(), greyFramePart.clone(),
				greyFramePart.clone() });
		if (mode == MODE.MODEL) {
			final List<ConnectedComponent> labels = labeler.findComponents(greyFramePart);
			if (labels.size() > 0) {
				final PixelSet c = largestLabel(labels);
				final Pixel centroid = c.calculateCentroidPixel();
				int distance = -1;
				if (this.homographyPoints.size() != 0) {
					distance = distance(homographyPoints.get(homographyPoints.size() - 1).firstObject(), centroid);
				}
				if (this.homographyPoints.size() == 0 || distance > screenDiagonal) {
					System.out.println("Point found at: " + centroid);
					System.out.println("Distance was: " + distance);
					final IndependentPair<String, Point2d> calibration = this.calibrationPoints
							.get(this.calibrationPointIndex);
					System.out.println("Adding point for: " + calibration.firstObject());
					this.homographyPoints.add(new Pair<Point2d>(centroid, calibration.secondObject()));
					this.calibrationPointIndex++;
					if (this.calibrationPointIndex >= this.calibrationPoints.size()) {
						this.homography.estimate(homographyPoints);
						this.mode = MODE.SEARCHING;
						drawingPanel.fill(RGBColour.WHITE);
					}
					else {
						System.out.println("CURRENTLY EXPECTING POINT: "
								+ this.calibrationPoints.get(this.calibrationPointIndex).firstObject());
					}
				}

			}
		}
		else if (mode == MODE.SEARCHING) {
			System.out.println("TOTALLY SEARHCING");
			final List<ConnectedComponent> labels = labeler.findComponents(greyFramePart);
			if (labels.size() > 0) {
				this.mode = MODE.LINE_CONSTRUCTING;
				final PixelSet c = largestLabel(labels);
				final Point2dImpl actualPoint = findActualPoint(c);
				drawingPanel.createRenderer().drawShapeFilled(new Circle((int) actualPoint.x, (int) actualPoint.y, 5),
						RGBColour.BLACK);
				previousPoint = actualPoint;
			}
		}
		else if (mode == MODE.LINE_CONSTRUCTING) {
			System.out.println("TOTALLY LINE DRAWING");
			final List<ConnectedComponent> labels = labeler.findComponents(greyFramePart);
			if (labels.size() > 0) {
				final PixelSet c = largestLabel(labels);
				final Point2dImpl actualPoint = findActualPoint(c);
				drawingPanel.createRenderer().drawLine(new Line2d(previousPoint, actualPoint), 5, RGBColour.BLACK);
				previousPoint = actualPoint;
			}
			else {
				mode = MODE.SEARCHING;
				previousPoint = null;
			}
		}
		frame.internalAssign(greyFrame);
	}

	private Point2dImpl findActualPoint(PixelSet c) {
		final double[] centroidDouble = c.calculateCentroid();
		final Point2dImpl centroid = new Point2dImpl((float) centroidDouble[0], (float) centroidDouble[1]);
		final Point2dImpl actualPoint = centroid.transform(this.homography.getTransform());
		return actualPoint;
	}

	private PixelSet largestLabel(List<ConnectedComponent> labels) {
		int max = 0;
		PixelSet r = null;
		for (final PixelSet c : labels) {
			if (c.getPixels().size() > max)
			{
				max = c.getPixels().size();
				r = c;
			}
		}
		return r;
	}

	private int distance(Point2d p1, Point2d p2) {
		final double dx = p1.getX() - p2.getX();
		final double dy = p1.getY() - p2.getY();
		return (int) Math.sqrt(dx * dx + dy * dy);
	}

	public static void main(String args[]) throws IOException {
		new DigitalWhiteboard();
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {

	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent event) {
		System.out.println("Got a key");
		if (event.getKeyChar() == 'c') {
			drawingPanel.fill(RGBColour.WHITE);
			System.out.println("Modelling mode started");
			this.mode = MODE.MODEL;
			this.calibrationPointIndex = 0;
			homographyPoints.clear();
			this.homography = new HomographyModel();

			System.out.println("CURRENTLY EXPECTING POINT: "
					+ this.calibrationPoints.get(this.calibrationPointIndex).firstObject());

		}
		if (event.getKeyChar() == 'd' && this.homographyPoints.size() > 4) {

		}
	}
}
