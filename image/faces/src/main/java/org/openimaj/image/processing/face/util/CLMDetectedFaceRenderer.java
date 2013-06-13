package org.openimaj.image.processing.face.util;

import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.face.detection.CLMDetectedFace;
import org.openimaj.image.processing.face.tracking.clm.MultiTracker;
import org.openimaj.image.processing.face.tracking.clm.MultiTracker.TrackedFace;
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
