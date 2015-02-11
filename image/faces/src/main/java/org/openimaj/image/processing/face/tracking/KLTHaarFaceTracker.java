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
package org.openimaj.image.processing.face.tracking;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.video.processing.tracking.BasicObjectTracker;
import org.openimaj.video.tracking.klt.KLTTracker;

/**
 * A face tracker that uses the {@link HaarCascadeDetector} to detect faces in
 * the image and then tracks them using the {@link KLTTracker}.
 *
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *
 * @created 13 Oct 2011
 */
public class KLTHaarFaceTracker implements FaceTracker<FImage> {
	/** The face detector used to detect the faces */
	private final HaarCascadeDetector faceDetector = new HaarCascadeDetector();

	/** A list of trackers that are tracking faces within the image */
	private final List<BasicObjectTracker> trackers = new ArrayList<BasicObjectTracker>();

	/** The previous frame */
	private FImage previousFrame = null;

	/** When all faces are lost, the frame is retried */
	private boolean retryFrame = false;

	/** The number of frames to force a retry */
	private int forceRetry = -1;

	/** Used for forcing retry */
	private int frameCounter = 0;

	private final float detectionScalar = 1.2f;

	/**
	 * Default constructor that takes the minimum size (in pixels) of detections
	 * that should be considered faces.
	 *
	 * @param minSize
	 *            The minimum size of face boxes
	 */
	public KLTHaarFaceTracker(final int minSize) {
		this.faceDetector.setMinSize(minSize);
	}

	/**
	 * Used to detect faces when there is no current state.
	 *
	 * @return The list of detected faces
	 */
	private List<DetectedFace> detectFaces(final FImage img) {
		return this.faceDetector.detectFaces(img);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.processing.face.tracking.FaceTracker#trackFace(org.openimaj.image.Image)
	 */
	@Override
	public List<DetectedFace> trackFace(final FImage img)
	{
		List<DetectedFace> detectedFaces = new ArrayList<DetectedFace>();

		// Determine whether we need to force a retry now
		if (this.forceRetry != -1 && this.frameCounter % this.forceRetry == 0)
			this.trackers.clear();

		// If we're just starting tracking, find some features and start
		// tracking them.
		if (this.previousFrame == null || this.trackers.size() == 0) {

			// Detect the faces in the image.
			final List<DetectedFace> faces = this.detectFaces(img);

			// Create trackers for each face found
			for (final DetectedFace face : faces) {
				// Create a new tracker for this face
				final BasicObjectTracker faceTracker = new BasicObjectTracker();
				final Rectangle r = face.getBounds();
				r.scaleCentroid(this.detectionScalar);
				faceTracker.initialiseTracking(r, img);
				this.trackers.add(faceTracker);

				// Store the last frame
				this.previousFrame = img;
			}

			detectedFaces = faces;
		} else
			// If we have a previous frame, attempt to track the frame
			if (this.previousFrame != null) {
				// Update all the trackers
				final Iterator<BasicObjectTracker> i = this.trackers.iterator();
				while (i.hasNext()) {
					final BasicObjectTracker tracker = i.next();
					if (tracker.trackObject(img).size() == 0)
						i.remove();
					else {
						// Store the bounding box of the tracked features as the
						// face
						detectedFaces
						.add(new DetectedFace(
								tracker.getFeatureList().getBounds(),
								img.extractROI(tracker.getFeatureList().getBounds()),
								tracker.getFeatureList().countRemainingFeatures()));

						// Store the last frame
						this.previousFrame = img;
					}
				}

				if (this.trackers.size() == 0 && this.retryFrame == false) {
					this.retryFrame = true;
					detectedFaces = this.trackFace(img);
				}
			}

		this.frameCounter++;
		this.retryFrame = false;
		return detectedFaces;
	}

	/**
	 * @return the forceRetry
	 */
	public int getForceRetry()
	{
		return this.forceRetry;
	}

	/**
	 * @param forceRetry
	 *            the forceRetry to set
	 */
	public void setForceRetry(final int forceRetry)
	{
		this.forceRetry = forceRetry;
	}
}
