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
package org.openimaj.image.processing.face.tracking.clm;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.face.tracking.clm.MultiTracker.TrackedFace;
import org.openimaj.image.processing.face.tracking.clm.MultiTracker.TrackerVars;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.shape.Triangle;

import Jama.Matrix;

import com.jsaragih.IO;
import com.jsaragih.Tracker;

/**
 * CLM-based face tracker
 *
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 */
public class CLMFaceTracker {
	/** The tracker to use */
	public MultiTracker model = null;

	/** The face mesh */
	public int[][] triangles = null;

	/** The face connections */
	public int[][] connections = null;

	/** The scale at which to process the video */
	public float scale = 1f;

	/** Whether to use the face check (using pixels as a face classifier) */
	public boolean fcheck = false;

	/** Number of frames on which to force a redetection */
	public int fpd = -1;

	/** Search window size while tracking */
	public int[] wSize1 = { 7 };

	/** Search window size when initialising after a failed track */
	public int[] wSize2 = { 11, 9, 7 };

	/** Number of iterations to use for model fitting */
	public int nIter = 5;

	/** Number of standard deviations from the mean face to allow in the model */
	public double clamp = 3;

	/** Model fitting optimisation tolerance */
	public double fTol = 0.01;

	/** Whether the last track failed */
	private boolean failed = true;

	/** The size of the search area for redetection (template matching) */
	public float searchAreaSize = 1.4f;

	/** Colour to draw the connections */
	private Float[] connectionColour = RGBColour.WHITE;

	/** Colour to draw the points */
	private Float[] pointColour = RGBColour.GREEN;

	/** Colour to draw the mesh */
	private Float[] meshColour = RGBColour.BLACK;

	/** Colour to draw the bounding box */
	private Float[] boundingBoxColour = RGBColour.RED;

	/** Colour to draw the search area */
	private Float[] searchAreaColour = RGBColour.YELLOW;

	/**
	 * Instantiates a tracker for tracking faces based on some default values
	 * and models.
	 */
	public CLMFaceTracker() {
		this.model = new MultiTracker(MultiTracker.load(Tracker.class
				.getResourceAsStream("face2.tracker")));
		this.triangles = IO.loadTri(Tracker.class.getResourceAsStream("face.tri"));
		this.connections = IO.loadCon(Tracker.class.getResourceAsStream("face.con"));
	}

	/**
	 * Track the face in the given frame.
	 *
	 * @param frame
	 *            The frame
	 */
	public void track(final MBFImage frame) {
		// Make a greyscale image
		final FImage im = frame.flatten();

		this.track(im);
	}

	/**
	 * Track the face in the given frame.
	 *
	 * @param im
	 *            The frame
	 */
	public void track(FImage im) {
		// If we're to rescale, let's do that first
		if (this.scale != 1)
			if (this.scale == 0.5f)
				im = ResizeProcessor.halfSize(im);
			else
				im = ResizeProcessor.resample(im, (int) (this.scale * im.width),
						(int) (this.scale * im.height));

		int[] wSize;
		if (this.failed)
			wSize = this.wSize2;
		else
			wSize = this.wSize1;

		// Track the face
		if (this.model.track(im, wSize, this.fpd, this.nIter, this.clamp, this.fTol, this.fcheck,
				this.searchAreaSize) == 0)
		{
			this.failed = false;
		} else {
			this.model.frameReset();
			this.failed = true;
		}
	}

	/**
	 * Force a reset on the next frame to be tracked.
	 */
	public void reset() {
		this.model.frameReset();
	}

	/**
	 * Draw the model onto the image
	 *
	 * @param image
	 *            The image to draw onto
	 * @param drawTriangles
	 *            Whether to draw the triangles
	 * @param drawConnections
	 *            Whether to draw the connections
	 * @param drawPoints
	 *            Whether to draw the points
	 * @param drawSearchArea
	 *            Whether to draw the search area
	 * @param drawBounds
	 *            Whether to draw the bounds
	 */
	public void drawModel(final MBFImage image, final boolean drawTriangles,
			final boolean drawConnections, final boolean drawPoints,
			final boolean drawSearchArea, final boolean drawBounds)
	{
		for (int fc = 0; fc < this.model.trackedFaces.size(); fc++) {
			final MultiTracker.TrackedFace f = this.model.trackedFaces.get(fc);

			if (drawSearchArea) {
				// Draw the search area size
				final Rectangle r = f.lastMatchBounds.clone();
				r.scaleCentroid(this.searchAreaSize);
				image.createRenderer().drawShape(r, RGBColour.YELLOW);
			}

			// Draw the face model
			CLMFaceTracker.drawFaceModel(image, f, drawTriangles, drawConnections, drawPoints,
					drawSearchArea, drawBounds, this.triangles, this.connections, this.scale,
					this.boundingBoxColour, this.meshColour, this.connectionColour,
					this.pointColour);
		}
	}

	/**
	 * Draw onto the given image, the given face model.
	 *
	 * @param image
	 *            The image to draw onto
	 * @param f
	 *            The face model to draw
	 * @param drawTriangles
	 *            Whether to draw the triangles
	 * @param drawConnections
	 *            Whether to draw the connections
	 * @param drawPoints
	 *            Whether to draw the points
	 * @param drawSearchArea
	 *            Whether to draw the search area
	 * @param drawBounds
	 *            Whether to draw the bounds
	 * @param triangles
	 *            The reference triangles
	 * @param connections
	 *            The reference connections
	 * @param scale
	 *            The scale at which to draw
	 * @param boundingBoxColour
	 *            Colour to draw the bounding box
	 * @param meshColour
	 *            Colour to draw the mesh
	 * @param connectionColour
	 *            Colour to draw the connections
	 * @param pointColour
	 *            Colour to draw the points
	 */
	public static void drawFaceModel(final MBFImage image, final MultiTracker.TrackedFace f,
			final boolean drawTriangles, final boolean drawConnections, final boolean drawPoints,
			final boolean drawSearchArea, final boolean drawBounds, final int[][] triangles,
			final int[][] connections, final float scale, final Float[] boundingBoxColour,
			final Float[] meshColour, final Float[] connectionColour, final Float[] pointColour)
	{
		final int n = f.shape.getRowDimension() / 2;
		final Matrix visi = f.clm._visi[f.clm.getViewIdx()];

		if (drawBounds && f.lastMatchBounds != null)
			image.createRenderer().drawShape(f.lastMatchBounds,
					boundingBoxColour);

		if (drawTriangles) {
			// Draw triangulation
			for (int i = 0; i < triangles.length; i++) {
				if (visi.get(triangles[i][0], 0) == 0 ||
					visi.get(triangles[i][1], 0) == 0 ||
					visi.get(triangles[i][2], 0) == 0
				) continue;

				final Triangle t = new Triangle(
					new Point2dImpl(
						(float) f.shape.get(triangles[i][0], 0) / scale,
						(float) f.shape.get(triangles[i][0] + n, 0) / scale),
					new Point2dImpl(
						(float) f.shape.get(triangles[i][1], 0) / scale,
						(float) f.shape.get(triangles[i][1] + n, 0) / scale),
					new Point2dImpl(
						(float) f.shape.get(triangles[i][2], 0) / scale,
						(float) f.shape.get(triangles[i][2] + n, 0) / scale)
				);
				image.drawShape(t, meshColour);
			}
		}

		if (drawConnections) {
			// draw connections
			for (int i = 0; i < connections[0].length; i++) {
				if (visi.get(connections[0][i], 0) == 0
						|| visi.get(connections[1][i], 0) == 0)
					continue;

				image.drawLine(
						new Point2dImpl((float) f.shape.get(connections[0][i],
								0) / scale, (float) f.shape.get(
								connections[0][i] + n, 0) / scale),
						new Point2dImpl((float) f.shape.get(connections[1][i],
								0) / scale, (float) f.shape.get(
								connections[1][i] + n, 0) / scale),
						connectionColour);
			}
		}

		if (drawPoints) {
			// draw points
			for (int i = 0; i < n; i++) {
				if (visi.get(i, 0) == 0)
					continue;

				image.drawPoint(new Point2dImpl((float) f.shape.get(i, 0)
						/ scale, (float) f.shape.get(i + n, 0) / scale),
						pointColour, 2);
			}
		}
	}

	/**
	 * Get the reference triangles
	 *
	 * @return The triangles
	 */
	public int[][] getReferenceTriangles() {
		return this.triangles;
	}

	/**
	 * Get the reference connections
	 *
	 * @return The connections
	 */
	public int[][] getReferenceConnections() {
		return this.connections;
	}

	/**
	 * Returns the model tracker
	 *
	 * @return The model tracker
	 */
	public MultiTracker getModelTracker() {
		return this.model;
	}

	/**
	 * Returns the initial variables that will be used by the tracker for each
	 * found face.
	 *
	 * @return The initial tracker variables.
	 */
	public TrackerVars getInitialVars() {
		return this.model.getInitialVars();
	}

	/**
	 * Initialises the face model for the tracked face by calling
	 * {@link MultiTracker#initShape(Rectangle, Matrix, Matrix)} with the
	 * rectangle of {@link TrackedFace#redetectedBounds} and the face shape and
	 * the reference shape. Assumes that the bounds have been already set up.
	 *
	 * @param face
	 *            The face to initialise
	 */
	public void initialiseFaceModel(final TrackedFace face) {
		this.model.initShape(face.redetectedBounds, face.shape,
				face.referenceShape);
	}

	/**
	 * @return the searchAreaSize
	 */
	public float getSearchAreaSize() {
		return this.searchAreaSize;
	}

	/**
	 * @param searchAreaSize
	 *            the searchAreaSize to set
	 */
	public void setSearchAreaSize(final float searchAreaSize) {
		this.searchAreaSize = searchAreaSize;
	}

	/**
	 * @return the connectionColour
	 */
	public Float[] getConnectionColour() {
		return this.connectionColour;
	}

	/**
	 * @param connectionColour
	 *            the connectionColour to set
	 */
	public void setConnectionColour(final Float[] connectionColour) {
		this.connectionColour = connectionColour;
	}

	/**
	 * @return the pointColour
	 */
	public Float[] getPointColour() {
		return this.pointColour;
	}

	/**
	 * @param pointColour
	 *            the pointColour to set
	 */
	public void setPointColour(final Float[] pointColour) {
		this.pointColour = pointColour;
	}

	/**
	 * @return the meshColour
	 */
	public Float[] getMeshColour() {
		return this.meshColour;
	}

	/**
	 * @param meshColour
	 *            the meshColour to set
	 */
	public void setMeshColour(final Float[] meshColour) {
		this.meshColour = meshColour;
	}

	/**
	 * @return the boundingBoxColour
	 */
	public Float[] getBoundingBoxColour() {
		return this.boundingBoxColour;
	}

	/**
	 * @param boundingBoxColour
	 *            the boundingBoxColour to set
	 */
	public void setBoundingBoxColour(final Float[] boundingBoxColour) {
		this.boundingBoxColour = boundingBoxColour;
	}

	/**
	 * @return the searchAreaColour
	 */
	public Float[] getSearchAreaColour() {
		return this.searchAreaColour;
	}

	/**
	 * @param searchAreaColour
	 *            the searchAreaColour to set
	 */
	public void setSearchAreaColour(final Float[] searchAreaColour) {
		this.searchAreaColour = searchAreaColour;
	}

	/**
	 * @return the list of tracked faces from the previous call to
	 *         {@link #track(MBFImage)} or {@link #track(FImage)}.
	 */
	public List<TrackedFace> getTrackedFaces() {
		return this.model.trackedFaces;
	}

	/**
	 * Get the triangle mesh corresponding to a tracked face.
	 *
	 * @param face
	 *            the {@link TrackedFace}
	 * @return the mesh
	 */
	public List<Triangle> getTriangles(final TrackedFace face) {
		return CLMFaceTracker.getTriangles(face.shape, face.clm._visi[face.clm.getViewIdx()], this.triangles);
	}

	/**
	 * Get the triangle mesh corresponding to a tracked face.
	 *
	 * @param shape
	 *            the shape matrix
	 * @param visi
	 *            the visibility matrix
	 * @param triangles
	 *            the triangle definitions
	 *
	 * @return the mesh
	 */
	public static List<Triangle> getTriangles(final Matrix shape, final Matrix visi, final int[][] triangles) {
		final int n = shape.getRowDimension() / 2;
		final List<Triangle> tris = new ArrayList<Triangle>();

		for (int i = 0; i < triangles.length; i++) {
			if (visi != null &&
					(visi.get(triangles[i][0], 0) == 0 ||
							visi.get(triangles[i][1], 0) == 0 ||
					visi.get(triangles[i][2], 0) == 0))
			{
				tris.add(null);
			} else {
				final Triangle t = new Triangle(
						new Point2dImpl((float) shape.get(triangles[i][0], 0),
								(float) shape.get(triangles[i][0] + n, 0)),
						new Point2dImpl((float) shape.get(triangles[i][1], 0),
								(float) shape.get(triangles[i][1] + n, 0)),
						new Point2dImpl((float) shape.get(triangles[i][2], 0),
								(float) shape.get(triangles[i][2] + n, 0))
						);
				tris.add(t);
			}
		}

		return tris;
	}

	/**
	 * 	Set the number of frames after which a redection is forced by the tracker.
	 * 	Set it to -1 to avoid forcing any redetection.
	 *	@param nFrames The number of frames.
	 */
	public void setRedetectEvery( final int nFrames )
	{
		this.fpd = nFrames;
	}
}
