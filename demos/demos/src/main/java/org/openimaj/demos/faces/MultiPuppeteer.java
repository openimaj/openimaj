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
package org.openimaj.demos.faces;

import gnu.trove.map.hash.TObjectIntHashMap;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import org.openimaj.demos.Demo;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.face.tracking.clm.CLMFaceTracker;
import org.openimaj.image.processing.face.tracking.clm.MultiTracker.TrackedFace;
import org.openimaj.image.processing.transform.PiecewiseMeshWarp;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.shape.Shape;
import org.openimaj.math.geometry.shape.Triangle;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.util.pair.Pair;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.capture.VideoCaptureException;

/**
 * Demo showing real-time face mapping.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
@Demo(
		author = "Jonathon Hare",
		description = "Real-time face mapping based on the CLM face tracker. Multiple faces are supported.",
		keywords = { "video", "face", "webcam", "constrained local model" },
		title = "Puppeteer")
public class MultiPuppeteer implements VideoDisplayListener<MBFImage> {
	private CLMFaceTracker tracker = new CLMFaceTracker();
	private List<IndependentPair<MBFImage, List<Triangle>>> puppets = new ArrayList<IndependentPair<MBFImage, List<Triangle>>>();
	private TObjectIntHashMap<TrackedFace> puppetAssignments = new TObjectIntHashMap<TrackedFace>();
	private int nextPuppet = 0;

	/**
	 * Default constructor.
	 * 
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public MultiPuppeteer() throws MalformedURLException, IOException {
		tracker.scale = 0.5f;
		tracker.fpd = 120;
		tracker.fcheck = false;

		final CLMFaceTracker ptracker = new CLMFaceTracker();

		final URL[] puppetUrls = {
				MultiPuppeteer.class.getResource("nigel.jpg"),
				MultiPuppeteer.class.getResource("wendy.png")
		};

		for (final URL url : puppetUrls) {
			MBFImage image = ImageUtilities.readMBF(url);

			final int paddingWidth = Math.max(image.getWidth(), 640);
			final int paddingHeight = Math.max(image.getHeight(), 480);

			image = image.padding(paddingWidth, paddingHeight);

			ptracker.track(image);

			final TrackedFace face = ptracker.getTrackedFaces().get(0);

			puppets.add(IndependentPair.pair(image, ptracker.getTriangles(face)));

			ptracker.reset();
		}
	}

	@Override
	public void afterUpdate(VideoDisplay<MBFImage> display) {
		// do nothing
	}

	@Override
	public void beforeUpdate(MBFImage frame) {
		tracker.track(frame);

		final List<TrackedFace> tracked = tracker.getTrackedFaces();

		for (final TrackedFace face : tracked) {
			int asgn;

			if (puppetAssignments.contains(face)) {
				asgn = puppetAssignments.get(face);
			} else {
				asgn = nextPuppet;
				puppetAssignments.put(face, asgn);
				nextPuppet++;

				if (nextPuppet >= puppets.size())
					nextPuppet = 0;
			}

			final List<Triangle> triangles = tracker.getTriangles(face);
			final IndependentPair<MBFImage, List<Triangle>> puppetData = this.puppets.get(asgn);
			final List<Triangle> puppetTriangles = puppetData.secondObject();

			final List<Pair<Shape>> matches = computeMatches(puppetTriangles, triangles);

			final PiecewiseMeshWarp<Float[], MBFImage> pmw = new PiecewiseMeshWarp<Float[], MBFImage>(matches);

			final Rectangle bounds = face.redetectedBounds.clone();
			bounds.height += 10;
			bounds.width += 10;
			bounds.x -= 5;
			bounds.y -= 5;
			bounds.scale((float) (1.0 / tracker.scale));

			final MBFImage puppet = puppetData.firstObject();
			final List<FImage> bands = puppet.bands;
			puppet.processInplace(pmw);

			composite(frame, puppet, bounds);

			puppet.bands = bands;
		}

		final Set<TrackedFace> toRemove = new HashSet<TrackedFace>(puppetAssignments.keySet());
		toRemove.removeAll(tracked);
		for (final TrackedFace face : toRemove) {
			puppetAssignments.remove(face);
		}
	}

	private List<Pair<Shape>> computeMatches(List<Triangle> from, List<Triangle> to) {
		final List<Pair<Shape>> mtris = new ArrayList<Pair<Shape>>();

		for (int i = 0; i < from.size(); i++) {
			final Triangle t1 = from.get(i);
			Triangle t2 = to.get(i);

			if (t1 != null && t2 != null) {
				t2 = t2.clone();
				t2.scale((float) (1.0 / tracker.scale));
				mtris.add(new Pair<Shape>(t1, t2));
			}
		}

		return mtris;
	}

	private void composite(MBFImage back, MBFImage fore, Rectangle bounds) {
		final float[][] rin = fore.bands.get(0).pixels;
		final float[][] gin = fore.bands.get(1).pixels;
		final float[][] bin = fore.bands.get(2).pixels;

		final float[][] rout = back.bands.get(0).pixels;
		final float[][] gout = back.bands.get(1).pixels;
		final float[][] bout = back.bands.get(2).pixels;

		final int xmin = (int) Math.max(0, bounds.x);
		final int ymin = (int) Math.max(0, bounds.y);

		final int ymax = (int) Math.min(Math.min(fore.getHeight(), back.getHeight()), bounds.y + bounds.height);
		final int xmax = (int) Math.min(Math.min(fore.getWidth(), back.getWidth()), bounds.x + bounds.width);

		for (int y = ymin; y < ymax; y++) {
			for (int x = xmin; x < xmax; x++) {
				if (rin[y][x] != 0 && gin[y][x] != 0 && bin[y][x] != 0) {
					rout[y][x] = rin[y][x];
					gout[y][x] = gin[y][x];
					bout[y][x] = bin[y][x];
				}
			}
		}
	}

	/**
	 * The main method.
	 * 
	 * @param args
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	public static void main(String[] args) throws MalformedURLException, IOException {
		try {
			final MultiPuppeteer puppeteer = new MultiPuppeteer();

			VideoDisplay.createVideoDisplay(new VideoCapture(640, 480)).addVideoListener(puppeteer);
		} catch (final VideoCaptureException e) {
			JOptionPane.showMessageDialog(null, "No video capture devices were found!");
		}
	}
}
