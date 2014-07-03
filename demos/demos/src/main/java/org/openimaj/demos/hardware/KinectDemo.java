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
package org.openimaj.demos.hardware;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.openimaj.demos.Demo;
import org.openimaj.hardware.kinect.KinectController;
import org.openimaj.hardware.kinect.KinectException;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourMap;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.renderer.MBFImageRenderer;
import org.openimaj.image.renderer.RenderHints;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.video.Video;
import org.openimaj.video.VideoDisplay;

/**
 * Kinect integration demo. Shows video and depth. Press t to toggle between rgb
 * and ir mode. Pressing w and x moves the device up or down. Pressing s levels
 * the device.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Demo(
		author = "Jonathon Hare",
		description = "Kinect integration demo. Shows video and depth. Press t " +
				"to toggle between rgb and ir mode. Pressing w and x moves the device " +
				"up or down. Pressing s levels the device.",
		keywords = { "kinect", "video" },
		title = "Kinect Integration",
		screenshot = "/org/openimaj/demos/screens/hardware/kinect.png",
		icon = "/org/openimaj/demos/icons/hardware/kinect.png")
public class KinectDemo extends Video<MBFImage> implements KeyListener {
	MBFImage currentFrame;
	KinectController controller;
	JFrame frame;
	private double tilt = 0;
	private boolean irmode = false;
	private final MBFImageRenderer renderer;
	private String accel;
	private final VideoDisplay<MBFImage> videoFrame;
	private boolean rdepth = true;
	private boolean printCloud = false;
	private MBFImage v3d;

	/**
	 * Default constructor
	 * 
	 * @param id
	 *            of kinect controller
	 * @throws KinectException
	 */
	public KinectDemo(int id) throws KinectException {
		controller = new KinectController(id, irmode, rdepth);
		currentFrame = new MBFImage(640 * 2, 480, ColourSpace.RGB);
		renderer = currentFrame.createRenderer(RenderHints.ANTI_ALIASED);

		videoFrame = VideoDisplay.createVideoDisplay(this);
		((JFrame) SwingUtilities.getRoot(videoFrame.getScreen())).setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		SwingUtilities.getRoot(videoFrame.getScreen()).addKeyListener(this);

		v3d = new MBFImage(640, 480);
	}

	@Override
	public MBFImage getNextFrame() {
		MBFImage vid;
		Image<?, ?> tmp = controller.videoStream.getNextFrame();

		if (tmp instanceof MBFImage) {
			vid = (MBFImage) tmp;
		} else {
			vid = new MBFImage((FImage) tmp, (FImage) tmp, (FImage) tmp);
		}

		renderer.drawImage(vid, 0, 0);

		tmp = controller.depthStream.getNextFrame();
		drawPointCloud((FImage) tmp, vid, 0, 0, 640, 440, 100, 80);

		MBFImage depth = null;
		if (this.rdepth) {
			final FImage fdepth = ((FImage) tmp).clone();
			if (printCloud) {
				printCloud = false;
				try {
					pointCloudOut(fdepth, "pointcloud.txt", 0, 0, 640, 440, 400, 320);
					System.out.println("Point cloud written!");
				} catch (final FileNotFoundException e) {
					System.err.println("failed to write pointcloud");
				}
			}
			final int pixToDraw = (int) fdepth.pixels[100][100];
			fdepth.normalise();
			depth = fdepth.toRGB();
			depth.drawText("Camera: " + Arrays.toString(new int[] { 100, 100, pixToDraw }), 0, 460,
					HersheyFont.TIMES_MEDIUM, 16, RGBColour.WHITE);
			depth.drawText("World: " + Arrays.toString(controller.cameraToWorld(100, 100, pixToDraw)), 0, 480,
					HersheyFont.TIMES_MEDIUM, 16, RGBColour.WHITE);
		} else {
			depth = ColourMap.Jet.apply((FImage) tmp);
		}

		renderer.drawImage(depth, 640, 0);

		if (super.currentFrame % 30 == 0)
			accel = controller.getAcceleration() + "";
		renderer.drawText(accel, 0, 480, HersheyFont.TIMES_MEDIUM, 16, RGBColour.WHITE);

		super.currentFrame++;

		return currentFrame;
	}

	private void pointCloudOut(FImage depth, String out, int xmin, int ymin, int xmax, int ymax, float xdiv, float ydiv)
			throws FileNotFoundException
	{
		final PrintWriter writer = new PrintWriter(new File(out));
		final float stepx = (xmax - xmin) / xdiv;
		final float stepy = (ymax - ymin) / ydiv;

		final float[] xyz = new float[3];
		final double factor = controller.computeScalingFactor();
		for (int y = ymin; y < ymax; y += stepy) {
			for (int x = xmin; x < xmax; x += stepx) {
				final int d = (int) depth.pixels[y][x];
				if (d > 0) {
					// double[] xyz = controller.cameraToWorld(x, y, d);
					controller.cameraToWorld(x, y, d, factor, xyz);
					writer.printf("%4.2f %4.2f %4.2f\n", xyz[0], xyz[1], xyz[2]);
				}
			}
			writer.flush();
		}
		writer.close();
	}

	private void drawPointCloud(FImage depth, MBFImage frame, int xmin, int ymin, int xmax, int ymax, float xdiv,
			float ydiv)
	{
		v3d.fill(RGBColour.BLACK);
		final List<Simple3D.Primative> points = new ArrayList<Simple3D.Primative>();

		final float stepx = 1;// (xmax - xmin) / xdiv;
		final float stepy = 1;// (ymax - ymin) / ydiv;

		float meanDepth = 0;
		int count = 0;

		final float[] xyz = new float[3];
		final double factor = controller.computeScalingFactor();
		for (int y = ymin; y < ymax; y += stepy) {
			for (int x = xmin; x < xmax; x += stepx) {
				final int d = (int) depth.pixels[y][x];
				if (d > 0) {
					// double[] xyz = controller.cameraToWorld(x, y, d);
					controller.cameraToWorld(x, y, d, factor, xyz);

					// writer.printf("%4.2f %4.2f %4.2f\n", xyz[0], xyz[1],
					// xyz[2]);
					points.add(new Simple3D.Point3D(xyz[0], -xyz[1], -xyz[2], frame.getPixel(x, y), 1));
					meanDepth -= xyz[2];
					count++;
				}
			}
		}

		meanDepth /= count;

		final double ax = Math.PI / 4;
		final Simple3D.Scene scene = new Simple3D.Scene(points);
		scene.translate(0, (int) (Math.tan(ax) * meanDepth), 0);
		scene.renderOrtho(Simple3D.euler2Rot(ax, 0, 0), v3d);
		DisplayUtilities.displayName(v3d, "3d");
	}

	@Override
	public MBFImage getCurrentFrame() {
		return currentFrame;
	}

	@Override
	public int getWidth() {
		return currentFrame.getWidth();
	}

	@Override
	public int getHeight() {
		return currentFrame.getHeight();
	}

	@Override
	public boolean hasNextFrame() {
		return true;
	}

	@Override
	public long countFrames() {
		return -1;
	}

	@Override
	public void reset() {
		// do nothing
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyChar() == 'w') {
			controller.setTilt(tilt += 1);
		} else if (e.getKeyChar() == 'x') {
			controller.setTilt(tilt -= 1);
		} else if (e.getKeyChar() == 's') {
			controller.setTilt(tilt = 0);
		} else if (e.getKeyChar() == 't') {
			controller.setIRMode(irmode = !irmode);
		} else if (e.getKeyChar() == 'y') {
			controller.setRegisteredDepth(rdepth = !rdepth);
		} else if (e.getKeyChar() == 'p') {
			printCloud = true;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {

	}

	/**
	 * Default main
	 * 
	 * @param args
	 *            Command-line arguments
	 */
	public static void main(String[] args) {
		try {
			new KinectDemo(0);
		} catch (final KinectException e) {
			JOptionPane.showMessageDialog(null, "No available Kinect device found!");
		}
	}

	@Override
	public long getTimeStamp() {
		return (long) (super.currentFrame * 1000 / getFPS());
	}

	@Override
	public double getFPS() {
		return 30;
	}

	/**
	 * Get the display showing the kinect video
	 * 
	 * @return The video display
	 */
	public VideoDisplay<MBFImage> getDisplay() {
		return videoFrame;
	}
}
