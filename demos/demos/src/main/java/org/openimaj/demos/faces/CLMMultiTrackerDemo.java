package org.openimaj.demos.faces;

import javax.swing.JOptionPane;

import org.openimaj.demos.Demo;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.face.tracking.clm.CLMFaceTracker;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.capture.VideoCaptureException;

/**
 * Demo for the {@link CLMFaceTracker} using a webcam.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 */
@Demo(
		author = "Jonathon Hare & David Dupplaw",
		description = "Tracking and fitting face models using a Constrained " +
				"Local Model. This demo supports tracking multiple faces.",
		keywords = { "video", "face", "webcam", "constrained local model" },
		title = "CLM Multi Face Tracker")
public class CLMMultiTrackerDemo implements VideoDisplayListener<MBFImage> {
	CLMFaceTracker tracker = new CLMFaceTracker();

	CLMMultiTrackerDemo() {
		tracker.fcheck = false;
	}

	@Override
	public void afterUpdate(VideoDisplay<MBFImage> display) {
		// do nothing
	}

	@Override
	public void beforeUpdate(MBFImage frame) {
		tracker.track(frame);

		tracker.drawModel(frame, true, true, true, true, true);
	}

	/**
	 * The main method.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			VideoDisplay.createVideoDisplay(new VideoCapture(640, 480)).addVideoListener(new CLMMultiTrackerDemo());
		} catch (final VideoCaptureException e) {
			JOptionPane.showMessageDialog(null, "No video capture devices were found!");
		}
	}
}
