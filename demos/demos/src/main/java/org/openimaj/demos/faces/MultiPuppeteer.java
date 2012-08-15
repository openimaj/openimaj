package org.openimaj.demos.faces;

import gnu.trove.map.hash.TObjectIntHashMap;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.face.tracking.clm.CLMFaceTracker;
import org.openimaj.image.processing.face.tracking.clm.MultiTracker.TrackedFace;
import org.openimaj.math.geometry.shape.Shape;
import org.openimaj.math.geometry.shape.Triangle;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.util.pair.Pair;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;

public class MultiPuppeteer implements VideoDisplayListener<MBFImage> {
	CLMFaceTracker tracker = new CLMFaceTracker();
	List<IndependentPair<MBFImage, List<Triangle>>> puppets = new ArrayList<IndependentPair<MBFImage, List<Triangle>>>();
	TObjectIntHashMap<TrackedFace> puppetAssignments = new TObjectIntHashMap<TrackedFace>();
	int nextPuppet = 0;

	public MultiPuppeteer() throws MalformedURLException, IOException {
		final String[] puppetUrls = {
				"http://www.oii.ox.ac.uk/images/people/large/nigel_shadbolt.jpg"
		};

		for (final String url : puppetUrls) {
			final MBFImage image = ImageUtilities.readMBF(new URL(url));

			tracker.track(image);

			final TrackedFace face = tracker.getTrackedFaces().get(0);

			puppets.add(IndependentPair.pair(image, tracker.getTriangles(face)));
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
			// if (puppetAssignments.containsKey(face)) {
			// final int asgn = puppetAssignments.get(face);
			//
			// } else {
			//
			// }

			final List<Triangle> triangles = tracker.getTriangles(face);

		}

		final Set<TrackedFace> toRemove = puppetAssignments.keySet();
		toRemove.removeAll(tracked);
		for (final TrackedFace face : toRemove) {
			puppetAssignments.remove(face);
		}
	}

	private List<Pair<Shape>> computeMatches(List<Triangle> referenceTriangles, List<Triangle> triangles) {
		final List<Pair<Shape>> mtris = new ArrayList<Pair<Shape>>();

		for (int i = 0; i < triangles.size(); i++) {
			final Triangle t1 = triangles.get(i);
			final Triangle t2 = referenceTriangles.get(i);

			if (t1 != null && t2 != null) {
				mtris.add(new Pair<Shape>(t1, t2));
			}
		}

		return mtris;
	}

	/**
	 * The main method.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// try {
		// VideoDisplay.createVideoDisplay(new VideoCapture(640,
		// 480)).addVideoListener(new MultiPuppeteer());
		// } catch (final VideoCaptureException e) {
		// JOptionPane.showMessageDialog(null,
		// "No video capture devices were found!");
		// }
	}
}
