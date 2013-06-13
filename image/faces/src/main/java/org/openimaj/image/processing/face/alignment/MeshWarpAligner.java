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
package org.openimaj.image.processing.face.alignment;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.face.detection.keypoints.FKEFaceDetector;
import org.openimaj.image.processing.face.detection.keypoints.FacialKeypoint;
import org.openimaj.image.processing.face.detection.keypoints.FacialKeypoint.FacialKeypointType;
import org.openimaj.image.processing.face.detection.keypoints.KEDetectedFace;
import org.openimaj.image.processing.transform.PiecewiseMeshWarp;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.shape.Shape;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.util.pair.Pair;

import Jama.Matrix;

/**
 * A MeshWarpAligner aligns facial images using a piecewise mesh warping such
 * that all detected facial keypoints are moved to their canonical coordinates.
 * The warping is accomplished by defining a mesh of triangles and
 * quadrilaterals over the facial keypoints and using bi-linear interpolation to
 * get corrected pixel values.
 * 
 * @see PiecewiseMeshWarp
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class MeshWarpAligner implements FaceAligner<KEDetectedFace> {
	// Define the default mesh
	private static final String[][] DEFAULT_MESH_DEFINITION = {
			{ "EYE_LEFT_RIGHT", "EYE_RIGHT_LEFT", "NOSE_MIDDLE" },
			{ "EYE_LEFT_LEFT", "EYE_LEFT_RIGHT", "NOSE_LEFT" },
			{ "EYE_RIGHT_RIGHT", "EYE_RIGHT_LEFT", "NOSE_RIGHT" },
			{ "EYE_LEFT_RIGHT", "NOSE_LEFT", "NOSE_MIDDLE" },
			{ "EYE_RIGHT_LEFT", "NOSE_RIGHT", "NOSE_MIDDLE" },
			{ "MOUTH_LEFT", "MOUTH_RIGHT", "NOSE_MIDDLE" },
			{ "MOUTH_LEFT", "NOSE_LEFT", "NOSE_MIDDLE" },
			{ "MOUTH_RIGHT", "NOSE_RIGHT", "NOSE_MIDDLE" },
			{ "MOUTH_LEFT", "NOSE_LEFT", "EYE_LEFT_LEFT" },
			{ "MOUTH_RIGHT", "NOSE_RIGHT", "EYE_RIGHT_RIGHT" },

			// { "P0", "EYE_LEFT_LEFT", "EYE_LEFT_RIGHT" },
			// { "P1", "EYE_RIGHT_RIGHT", "EYE_RIGHT_LEFT" },
			// { "P0", "EYE_LEFT_RIGHT", "EYE_RIGHT_LEFT", "P1" },
			// { "P3", "MOUTH_LEFT", "MOUTH_RIGHT", "P2" },

			// { "P0", "EYE_LEFT_LEFT", "MOUTH_LEFT" },
			// { "P1", "EYE_RIGHT_RIGHT", "MOUTH_RIGHT" },
			// {"P0", "P3", "MOUTH_LEFT"},
			// {"P1", "P2", "MOUTH_RIGHT"},

			// { "P0", "EYE_LEFT_RIGHT", "EYE_RIGHT_LEFT" },
			// { "P1", "EYE_RIGHT_LEFT", "EYE_LEFT_RIGHT" },
			//
			// { "P3", "MOUTH_LEFT", "MOUTH_RIGHT" },
			// { "P2", "MOUTH_RIGHT", "MOUTH_LEFT" },

			// {"P3", "EYE_LEFT_LEFT", "MOUTH_LEFT"},
			// {"P2", "EYE_RIGHT_RIGHT", "MOUTH_RIGHT"},
	};

	// Define the outer edges
	private static final Point2d P0 = new Point2dImpl(0, 0);
	private static final Point2d P1 = new Point2dImpl(80, 0);
	private static final Point2d P2 = new Point2dImpl(80, 80);
	private static final Point2d P3 = new Point2dImpl(0, 80);

	// Define the canonical point positions
	private static FacialKeypoint[] canonical = loadCanonicalPoints();

	// Define the mesh
	String[][] meshDefinition = DEFAULT_MESH_DEFINITION;

	FImage mask;

	/**
	 * Default constructor
	 */
	public MeshWarpAligner() {
		this(DEFAULT_MESH_DEFINITION);
	}

	/**
	 * Construct with the given mesh definition
	 * 
	 * @param meshDefinition
	 *            The mesh definition
	 */
	public MeshWarpAligner(String[][] meshDefinition) {
		this.meshDefinition = meshDefinition;

		final List<Pair<Shape>> mesh = createMesh(canonical);

		// build mask by mapping the canonical coords to themselves on a white
		// image
		mask = new FImage((int) P2.getX(), (int) P2.getY());
		mask.fill(1f);
		mask = mask.processInplace(new PiecewiseMeshWarp<Float, FImage>(mesh));
	}

	private static FacialKeypoint[] loadCanonicalPoints() {
		final FacialKeypoint[] points = new FacialKeypoint[AffineAligner.Pmu[0].length];

		for (int i = 0; i < points.length; i++) {
			points[i] = new FacialKeypoint(FacialKeypointType.valueOf(i));
			points[i].position = new Point2dImpl(2 * AffineAligner.Pmu[0][i] - 40, 2 * AffineAligner.Pmu[1][i] - 40);
		}

		return points;
	}

	protected FacialKeypoint[] getActualPoints(FacialKeypoint[] keys, Matrix tf0) {
		final FacialKeypoint[] points = new FacialKeypoint[AffineAligner.Pmu[0].length];

		for (int i = 0; i < points.length; i++) {
			points[i] = new FacialKeypoint(FacialKeypointType.valueOf(i));
			points[i].position = new Point2dImpl(
					FacialKeypoint.getKeypoint(keys, FacialKeypointType.valueOf(i)).position.transform(tf0));
		}

		return points;
	}

	protected List<Pair<Shape>> createMesh(FacialKeypoint[] det) {
		final List<Pair<Shape>> shapes = new ArrayList<Pair<Shape>>();

		for (final String[] vertDefs : meshDefinition) {
			final Polygon p1 = new Polygon();
			final Polygon p2 = new Polygon();

			for (final String v : vertDefs) {
				p1.getVertices().add(lookupVertex(v, det));
				p2.getVertices().add(lookupVertex(v, canonical));
			}
			shapes.add(new Pair<Shape>(p1, p2));
		}

		return shapes;
	}

	private Point2d lookupVertex(String v, FacialKeypoint[] pts) {
		if (v.equals("P0"))
			return P0;
		if (v.equals("P1"))
			return P1;
		if (v.equals("P2"))
			return P2;
		if (v.equals("P3"))
			return P3;

		return FacialKeypoint.getKeypoint(pts, FacialKeypointType.valueOf(v)).position;
	}

	@Override
	public FImage align(KEDetectedFace descriptor) {
		final float scalingX = P2.getX() / descriptor.getFacePatch().width;
		final float scalingY = P2.getY() / descriptor.getFacePatch().height;
		final Matrix tf0 = TransformUtilities.scaleMatrix(scalingX, scalingY);
		final Matrix tf = tf0.inverse();

		final FImage J = FKEFaceDetector.pyramidResize(descriptor.getFacePatch(), tf);
		final FImage smallpatch = FKEFaceDetector.extractPatch(J, tf, 80, 0);

		return getWarpedImage(descriptor.getKeypoints(), smallpatch, tf0);
	}

	protected FImage getWarpedImage(FacialKeypoint[] kpts, FImage patch, Matrix tf0) {
		final FacialKeypoint[] det = getActualPoints(kpts, tf0);
		final List<Pair<Shape>> mesh = createMesh(det);

		final FImage newpatch = patch.process(new PiecewiseMeshWarp<Float, FImage>(mesh));

		return newpatch;
	}

	@Override
	public FImage getMask() {
		return mask;
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		int sz = in.readInt();
		meshDefinition = new String[sz][];
		for (int i = 0; i < meshDefinition.length; i++) {
			sz = in.readInt();
			meshDefinition[i] = new String[sz];
			for (int j = 0; j < meshDefinition[i].length; j++)
				meshDefinition[i][j] = in.readUTF();
		}

		mask = ImageUtilities.readF(in);
	}

	@Override
	public byte[] binaryHeader() {
		return this.getClass().getName().getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeInt(meshDefinition.length);
		for (final String[] def : meshDefinition) {
			out.writeInt(def.length);
			for (final String s : def)
				out.writeUTF(s);
		}

		ImageUtilities.write(mask, "png", out);
	}
}
