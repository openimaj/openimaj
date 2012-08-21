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

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.openimaj.demos.Demo;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processing.transform.PiecewiseMeshWarp;
import org.openimaj.math.geometry.shape.Shape;
import org.openimaj.math.geometry.shape.Triangle;
import org.openimaj.util.pair.Pair;

/**
 * Demonstrate the {@link PiecewiseMeshWarp}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
@Demo(
		author = "Jonathon Hare",
		description = "Demonstrates the OpenIMAJ piecewise mesh image warp processor. " +
				"On the displayed image, drag the mouse to move the warp point.",
		keywords = { "image", "distortion", "warp", "non-linear" },
		title = "Non-Linear Image Warp",
		icon = "/org/openimaj/demos/icons/image/bird-icon.png")
public class PiecewiseMeshWarpDemo implements MouseMotionListener {
	private JFrame frame;
	private MBFImage img;

	/**
	 * Construct the demo
	 * 
	 * @throws IOException
	 */
	public PiecewiseMeshWarpDemo() throws IOException {
		img = ImageUtilities.readMBF(getClass().getResource("/org/openimaj/demos/image/bird.png"));
		frame = DisplayUtilities.displaySimple(img);

		frame.addMouseMotionListener(this);
	}

	protected void updateImage(Pixel newCentre) {
		final Pixel p1 = new Pixel(0, 0);
		final Pixel p2 = new Pixel(img.getWidth(), 0);
		final Pixel p3 = new Pixel(img.getWidth(), img.getHeight());
		final Pixel p4 = new Pixel(0, img.getHeight());
		final Pixel p5 = new Pixel(img.getWidth() / 2, img.getHeight() / 2);

		final Pixel np1 = new Pixel(0, 0);
		final Pixel np2 = new Pixel(img.getWidth(), 0);
		final Pixel np3 = new Pixel(img.getWidth(), img.getHeight());
		final Pixel np4 = new Pixel(0, img.getHeight());
		final Pixel np5 = newCentre;

		final List<Pair<Shape>> matchingRegions = new ArrayList<Pair<Shape>>();
		matchingRegions.add(new Pair<Shape>(new Triangle(p1, p2, p5), new Triangle(np1, np2, np5)));
		matchingRegions.add(new Pair<Shape>(new Triangle(p2, p3, p5), new Triangle(np2, np3, np5)));
		matchingRegions.add(new Pair<Shape>(new Triangle(p3, p4, p5), new Triangle(np3, np4, np5)));
		matchingRegions.add(new Pair<Shape>(new Triangle(p4, p1, p5), new Triangle(np4, np1, np5)));

		DisplayUtilities.display(img.process(new PiecewiseMeshWarp<Float[], MBFImage>(matchingRegions)), frame);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		final Pixel p = new Pixel(e.getX(), e.getY());
		updateImage(p);
	}

	@Override
	public void mouseMoved(MouseEvent e) {

	}

	/**
	 * The main method
	 * 
	 * @param args
	 *            ignored
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		new PiecewiseMeshWarpDemo();
	}
}
