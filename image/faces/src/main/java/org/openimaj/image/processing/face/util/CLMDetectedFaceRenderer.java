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
package org.openimaj.image.processing.face.util;

import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.face.detection.CLMDetectedFace;
import org.openimaj.image.processing.face.tracking.clm.MultiTracker;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.shape.Triangle;

import Jama.Matrix;

import com.jsaragih.IO;
import com.jsaragih.Tracker;

/**
 * Renderer for drawing {@link CLMDetectedFace}s
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class CLMDetectedFaceRenderer implements DetectedFaceRenderer<CLMDetectedFace> {
	private int[][] triangles;
	private boolean drawTriangles = true;
	private boolean drawConnections = true;
	private boolean drawPoints = true;
	private boolean drawBounds = true;
	private int[][] connections;
	private float scale = 1f;
	private Float[] boundingBoxColour = RGBColour.RED;
	private Float[] pointColour = RGBColour.BLUE;
	private Float[] meshColour = RGBColour.GREEN;
	private Float[] connectionColour = RGBColour.YELLOW;
	private int thickness;

	/**
	 * Loads the triangles and connections used to render
	 */
	public CLMDetectedFaceRenderer() {
		this.triangles = IO.loadTri(Tracker.class.getResourceAsStream("face.tri"));
		connections = IO.loadCon(Tracker.class.getResourceAsStream("face.con"));
	}

	@Override
	public void drawDetectedFace(MBFImage image, int thickness, CLMDetectedFace f) {
		this.thickness = thickness;
		drawFaceModel(image, f.getShapeMatrix(), f.getVisibility(), f.getBounds());
	}

	/**
	 * Helper function, does the same as
	 * {@link #drawDetectedFace(MBFImage,int, CLMDetectedFace)} but with the
	 * insides of a {@link TrackedFace}.
	 * 
	 * @param image
	 * @param f
	 */
	public void drawDetectedFace(MBFImage image, MultiTracker.TrackedFace f) {
		drawFaceModel(image, f.shape, f.clm._visi[f.clm.getViewIdx()], f.lastMatchBounds);
	}

	private void drawFaceModel(MBFImage image, Matrix shape, Matrix visi, Rectangle bounds)
	{
		final int n = shape.getRowDimension() / 2;

		if (drawBounds && bounds != null)
			image.createRenderer().drawShape(bounds,
					boundingBoxColour);

		if (drawTriangles) {
			// Draw triangulation
			for (int i = 0; i < triangles.length; i++) {
				if (visi.get(triangles[i][0], 0) == 0 ||
						visi.get(triangles[i][1], 0) == 0 ||
						visi.get(triangles[i][2], 0) == 0)
					continue;

				final Triangle t = new Triangle(
						new Point2dImpl(
								(float) (shape.get(triangles[i][0], 0) + bounds.x) / scale,
								(float) (shape.get(triangles[i][0] + n, 0) + bounds.y) / scale),
						new Point2dImpl(
								(float) (shape.get(triangles[i][1], 0) + bounds.x) / scale,
								(float) (shape.get(triangles[i][1] + n, 0) + bounds.y) / scale),
						new Point2dImpl(
								(float) (shape.get(triangles[i][2], 0) + bounds.x) / scale,
								(float) (shape.get(triangles[i][2] + n, 0) + bounds.y) / scale)
						);
				image.drawShape(t, thickness, meshColour);
			}
		}

		if (drawConnections) {
			// draw connections
			for (int i = 0; i < connections[0].length; i++) {
				if (visi.get(connections[0][i], 0) == 0
						|| visi.get(connections[1][i], 0) == 0)
					continue;

				image.drawLine(
						new Point2dImpl(
								(float) (shape.get(connections[0][i], 0) + bounds.x) / scale,
								(float) (shape.get(connections[0][i] + n, 0) + bounds.y) / scale),
						new Point2dImpl(
								(float) (shape.get(connections[1][i], 0) + bounds.x) / scale,
								(float) (shape.get(connections[1][i] + n, 0) + bounds.y) / scale),
						thickness, connectionColour);
			}
		}

		if (drawPoints) {
			// draw points
			for (int i = 0; i < n; i++) {
				if (visi.get(i, 0) == 0)
					continue;

				image.drawPoint(
						new Point2dImpl(
								((float) shape.get(i, 0) + bounds.x) / scale,
								((float) shape.get(i + n, 0) + bounds.y) / scale),
						pointColour, thickness);
			}
		}
	}

	/**
	 * Static helper function for quick and dirty rendering
	 * 
	 * @param mbf
	 *            image to draw on to
	 * @param thickness
	 *            line thickness
	 * @param face
	 *            face to draw
	 */
	public static void render(MBFImage mbf, int thickness, CLMDetectedFace face) {
		final CLMDetectedFaceRenderer render = new CLMDetectedFaceRenderer();
		render.drawDetectedFace(mbf, thickness, face);
	}

}
