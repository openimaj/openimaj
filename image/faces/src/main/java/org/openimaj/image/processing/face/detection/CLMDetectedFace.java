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
package org.openimaj.image.processing.face.detection;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.feature.DoubleFV;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.face.tracking.clm.MultiTracker.TrackedFace;
import org.openimaj.image.processing.face.tracking.clm.MultiTracker.TrackerVars;
import org.openimaj.io.IOUtils;
import org.openimaj.math.geometry.shape.Rectangle;

import Jama.Matrix;

/**
 * A constrained local model detected face. In addition to the patch and
 * detection rectangle, also provides the shape matrix (describing the 2D point
 * positions in the patch image), and the weight vectors for the model pose
 * (relative to the detection image) and shape.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Reference(
		type = ReferenceType.Inproceedings,
		author = { "Jason M. Saragih", "Simon Lucey", "Jeffrey F. Cohn" },
		title = "Face alignment through subspace constrained mean-shifts",
		year = "2009",
		booktitle = "IEEE 12th International Conference on Computer Vision, ICCV 2009, Kyoto, Japan, September 27 - October 4, 2009",
		pages = { "1034", "1041" },
		publisher = "IEEE",
		customData = {
				"doi", "http://dx.doi.org/10.1109/ICCV.2009.5459377",
				"researchr", "http://researchr.org/publication/SaragihLC09",
				"cites", "0",
				"citedby", "0"
		})
public class CLMDetectedFace extends DetectedFace {
	private Matrix shape;
	private Matrix poseParameters;
	private Matrix shapeParameters;
	private Matrix visibility;

	protected CLMDetectedFace() {
	}

	/**
	 * Construct a {@link CLMDetectedFace} by copying the state from a
	 * {@link TrackedFace}
	 *
	 * @param face
	 *            the {@link TrackedFace}
	 * @param image
	 *            the image in which the tracked face was detected
	 */
	public CLMDetectedFace(final TrackedFace face, final FImage image) {
		this(face.redetectedBounds, face.shape.copy(), face.clm._pglobl.copy(), face.clm._plocal.copy(),
				face.clm._visi[face.clm.getViewIdx()].copy(), image);
	}

	/**
	 * Construct with the given bounds, shape and pose parameters and detection
	 * image. The face patch is extracted automatically.
	 *
	 * @param bounds
	 * @param shape
	 * @param poseParameters
	 * @param shapeParameters
	 * @param visibility
	 * @param fullImage
	 */
	public CLMDetectedFace(final Rectangle bounds, final Matrix shape, final Matrix poseParameters, final Matrix shapeParameters,
			final Matrix visibility, final FImage fullImage)
	{
		super(bounds, fullImage.extractROI(bounds), 1);

		this.poseParameters = poseParameters;
		this.shapeParameters = shapeParameters;
		this.visibility = visibility;

		this.shape = shape;

		// translate the shape
		final int n = shape.getRowDimension() / 2;
		final double[][] shapeData = shape.getArray();
		for (int i = 0; i < n; i++) {
			shapeData[i][0] -= bounds.x;
			shapeData[i + n][0] -= bounds.y;
		}
	}



	/**
	 * Helper method to convert a list of {@link TrackedFace}s to
	 * {@link CLMDetectedFace}s.
	 *
	 * @param faces
	 *            the {@link TrackedFace}s.
	 * @param image
	 *            the image the {@link TrackedFace}s came from.
	 * @return the list of {@link CLMDetectedFace}s
	 */
	public static List<CLMDetectedFace> convert(final List<TrackedFace> faces, final MBFImage image) {
		final FImage fimage = image.flatten();

		return CLMDetectedFace.convert(faces, fimage);
	}

	/**
	 * Helper method to convert a list of {@link TrackedFace}s to
	 * {@link CLMDetectedFace}s.
	 *
	 * @param faces
	 *            the {@link TrackedFace}s.
	 * @param image
	 *            the image the {@link TrackedFace}s came from.
	 * @return the list of {@link CLMDetectedFace}s
	 */
	public static List<CLMDetectedFace> convert(final List<TrackedFace> faces, final FImage image) {
		final List<CLMDetectedFace> cvt = new ArrayList<CLMDetectedFace>();

		for (final TrackedFace f : faces) {
			cvt.add(new CLMDetectedFace(f, image));
		}

		return cvt;
	}

	/**
	 * 	Helper method that converts this {@link CLMDetectedFace} into
	 * 	a {@link TrackedFace}.
	 *	@return A {@link TrackedFace}
	 */
	public TrackedFace convert()
	{
		final TrackerVars tv = new TrackerVars();
		tv.clm._pglobl = this.poseParameters.copy();
		tv.clm._plocal = this.shapeParameters.copy();
		tv.shape = this.shape.copy();
		tv.clm._visi[ tv.clm.getViewIdx() ] = this.visibility.copy();
		return new TrackedFace( this.bounds, tv );
	}

	@Override
	public void writeBinary(final DataOutput out) throws IOException {
		super.writeBinary(out);

		IOUtils.write(this.getShape(), out);
		IOUtils.write(this.poseParameters, out);
		IOUtils.write(this.shapeParameters, out);
	}

	@Override
	public byte[] binaryHeader() {
		return "DF".getBytes();
	}

	@Override
	public void readBinary(final DataInput in) throws IOException {
		super.readBinary(in);
		this.shape = IOUtils.read(in);
		this.poseParameters = IOUtils.read(in);
		this.shapeParameters = IOUtils.read(in);
	}

	/**
	 * Returns the scale (size) of the face
	 * @return the scale of the model
	 */
	public double getScale() {
		return this.poseParameters.get(0, 0);
	}

	/**
	 * Returns the pitch of the model (that is the look up/down, noddy head movement).
	 * @return the pitch of the model
	 */
	public double getPitch() {
		return this.poseParameters.get(1, 0);
	}

	/**
	 * Returns the yaw of the face (that is the side-to-side, shakey head movement).
	 * @return the yaw of the model
	 */
	public double getYaw() {
		return this.poseParameters.get(2, 0);
	}

	/**
	 * Returns the roll of the model (that is the spinning, standy on the head movement)
	 * @return the roll of the model
	 */
	public double getRoll() {
		return this.poseParameters.get(3, 0);
	}

	/**
	 * Returns the x-translation in the model
	 * @return the x-translation of the model
	 */
	public double getTranslationX() {
		return this.poseParameters.get(4, 0);
	}

	/**
	 * Returns the y-translation in the model
	 * @return the y-translation of the model
	 */
	public double getTranslationY() {
		return this.poseParameters.get(5, 0);
	}

	/**
	 * Get the parameters describing the pose of the face. This doesn't include
	 * the translation or scale. The values are {pitch, yaw, roll}
	 *
	 * @return the pose parameters
	 */
	public DoubleFV getPoseParameters() {
		return new DoubleFV(new double[] { this.getPitch(), this.getYaw(), this.getRoll() });
	}

	/**
	 * Get the parameters describing the shape model (i.e. the weights for the
	 * eigenvectors of the point distribution model)
	 *
	 * @return the shape parameters
	 */
	public DoubleFV getShapeParameters() {
		final int len = this.shapeParameters.getRowDimension();
		final double[] vector = new double[len];

		for (int i = 0; i < len; i++) {
			vector[i] = this.shapeParameters.get(i, 0);
		}

		return new DoubleFV(vector);
	}

	/**
	 * Get a vector describing the pose (pitch, yaw and roll only) and shape of
	 * the model.
	 *
	 * @return the combined pose and shape vector
	 */
	public DoubleFV getPoseShapeParameters() {
		final int len = this.shapeParameters.getRowDimension();
		final double[] vector = new double[len + 3];

		vector[0] = this.getPitch();
		vector[1] = this.getYaw();
		vector[2] = this.getRoll();

		for (int i = 3; i < len + 3; i++) {
			vector[i] = this.shapeParameters.get(i, 0);
		}

		return new DoubleFV(vector);
	}

	/**
	 * Get the matrix of points describing the model. The points are relative to
	 * the image given by {@link #getFacePatch()}.
	 *
	 * @return the shape matrix
	 */
	public Matrix getShapeMatrix() {
		return this.shape;
	}

	/**
	 * Get the visibility matrix
	 *
	 * @return the visibility matrix
	 */
	public Matrix getVisibility() {
		return this.visibility;
	}
}
