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
import org.openimaj.image.processing.face.detection.CLMDetectedFace;
import org.openimaj.image.processing.face.detection.CLMFaceDetector.Configuration;
import org.openimaj.image.processing.face.tracking.clm.CLMFaceTracker;
import org.openimaj.image.processing.transform.PiecewiseMeshWarp;
import org.openimaj.io.IOUtils;
import org.openimaj.math.geometry.shape.Shape;
import org.openimaj.math.geometry.shape.Triangle;
import org.openimaj.util.pair.Pair;

/**
 * An aligner that warps a {@link CLMDetectedFace} to the neutral pose
 * (reference shape) of the {@link Configuration}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class CLMAligner implements FaceAligner<CLMDetectedFace> {
	private Configuration config;
	private int size = 100;
	private transient List<Triangle> referenceTriangles;
	private transient FImage mask;

	/**
	 * Construct a new {@link CLMAligner} using the default
	 * {@link Configuration} and default size of 100 pixels.
	 */
	public CLMAligner() {
		config = new Configuration();
		loadReference();
	}

	/**
	 * Construct a new {@link CLMAligner} using the default
	 * {@link Configuration} and given size for the aligned output image.
	 * 
	 * @param size
	 *            the output facial patch size
	 */
	public CLMAligner(int size) {
		this.size = size;
		config = new Configuration();
		loadReference();
	}

	private void loadReference() {
		referenceTriangles = CLMFaceTracker.getTriangles(config.referenceShape, null, this.config.triangles);

		mask = new FImage(size, size);

		for (final Triangle t : referenceTriangles) {
			// magic numbers chosen to scale and centre the face
			// with a small border
			t.scale(0.3f * size);
			t.translate(0.5f * size, 0.45f * size);

			mask.drawShapeFilled(t, 1f);
		}
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		config = IOUtils.read(in);
		loadReference();
	}

	@Override
	public byte[] binaryHeader() {
		return this.getClass().getName().getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		IOUtils.write(config, out);
	}

	@Override
	public FImage align(CLMDetectedFace face) {
		if (face == null)
			return null;

		final List<Triangle> triangles = CLMFaceTracker.getTriangles(
				face.getShapeMatrix(), face.getVisibility(), this.config.triangles);
		final List<Pair<Shape>> matches = computeMatches(triangles);

		final PiecewiseMeshWarp<Float, FImage> pmw = new
				PiecewiseMeshWarp<Float, FImage>(matches);

		return pmw.transform(face.getFacePatch(), size, size);
	}

	@Override
	public FImage getMask() {
		return mask;
	}

	private List<Pair<Shape>> computeMatches(List<Triangle> triangles) {
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
}
