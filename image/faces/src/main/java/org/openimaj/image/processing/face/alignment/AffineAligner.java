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

import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.face.detection.keypoints.FKEFaceDetector;
import org.openimaj.image.processing.face.detection.keypoints.FacialKeypoint;
import org.openimaj.image.processing.face.detection.keypoints.KEDetectedFace;

import Jama.Matrix;

/**
 * The {@link AffineAligner} attempts to find an affine transform that will warp
 * the face to the canonical frame by aligning facial keypoints.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class AffineAligner implements FaceAligner<KEDetectedFace> {
	/**
	 * Normalised positions of facial parts
	 */
	protected final static float[][] Pmu = {
			{ 25.0347f, 34.1802f, 44.1943f, 53.4623f, 34.1208f, 39.3564f, 44.9156f, 31.1454f, 47.8747f },
			{ 34.1580f, 34.1659f, 34.0936f, 33.8063f, 45.4179f, 47.0043f, 45.3628f, 53.0275f, 52.7999f } };

	final static int CANONICAL_SIZE = 80;

	int facePatchWidth = 80;
	int facePatchHeight = 80;
	float facePatchBorderPercentage = 0.225f;

	private FImage mask;

	/**
	 * Default Constructor with the default mask (80x80) and default border
	 * percentage (0.225).
	 */
	public AffineAligner() {
		this(loadDefaultMask());
	};

	/**
	 * Construct with a mask (in the canonical frame) to apply after aligning
	 * and default border percentage (0.225).
	 * 
	 * @param mask
	 */
	public AffineAligner(FImage mask) {
		this.mask = mask;
	}

	/**
	 * Construct with a mask (in the canonical frame) to apply after aligning
	 * and default border percentage (0.225).
	 * 
	 * @param mask
	 *            the mask
	 * @param facePatchBorderPercentage
	 *            the proportional size (against the detection patch) of the
	 *            border for the crop. Higher values result in a more zoomed-in
	 *            face.
	 */
	public AffineAligner(FImage mask, float facePatchBorderPercentage) {
		this.mask = mask;
		this.facePatchBorderPercentage = facePatchBorderPercentage;
		this.facePatchHeight = mask.height;
		this.facePatchWidth = mask.width;
	}

	/**
	 * Construct with no mask and the given size and border.
	 * 
	 * @param facePatchWidth
	 *            the width of the desired aligned faces
	 * @param facePatchHeight
	 *            the height of the desired aligned faces
	 * @param facePatchBorderPercentage
	 *            the proportional size (against the detection patch) of the
	 *            border for the crop. Higher values result in a more zoomed-in
	 *            face.
	 */
	public AffineAligner(int facePatchWidth, int facePatchHeight, float facePatchBorderPercentage) {
		this.mask = new FImage(facePatchWidth, facePatchHeight);
		mask.fill(1f);
		this.facePatchBorderPercentage = facePatchBorderPercentage;
		this.facePatchWidth = facePatchWidth;
		this.facePatchHeight = facePatchHeight;
	}

	@Override
	public FImage align(KEDetectedFace descriptor) {
		final int facePatchSize = Math.max(facePatchWidth, facePatchHeight);
		final double size = facePatchSize + 2.0 * facePatchSize * facePatchBorderPercentage;
		final double sc = CANONICAL_SIZE / size;

		// do the scaling to everything but the translation!!!
		final Matrix T = estimateAffineTransform(descriptor);
		T.set(0, 0, T.get(0, 0) * sc);
		T.set(1, 1, T.get(1, 1) * sc);
		T.set(0, 1, T.get(0, 1) * sc);
		T.set(1, 0, T.get(1, 0) * sc);

		final FImage J = FKEFaceDetector.pyramidResize(descriptor.getFacePatch(), T);
		final FImage bigPatch = FKEFaceDetector.extractPatch(J, T, (int) size,
				(int) (facePatchSize * facePatchBorderPercentage));

		return bigPatch.extractCenter(facePatchWidth, facePatchHeight).extractROI(0, 0, facePatchWidth, facePatchHeight)
				.multiplyInplace(mask);
	}

	/**
	 * Estimate the affine transform required to warp a set of facial keypoints
	 * to their canonical coordinates.
	 * <p>
	 * Affine transform is from a flat, vertically oriented (canonical) face to
	 * located face space. You'll need to invert this if you want to use it to
	 * extract the face from the image.
	 * 
	 * @param face
	 *            the face
	 * @return the affine transform matrix
	 */
	public static Matrix estimateAffineTransform(KEDetectedFace face) {
		return estimateAffineTransform(face.getKeypoints());
	}

	protected static Matrix estimateAffineTransform(FacialKeypoint[] pts) {
		float emin = Float.POSITIVE_INFINITY;
		Matrix T = null;

		for (int c = 0; c < 9; c++) {
			final Matrix A = new Matrix(8, 3);
			final Matrix B = new Matrix(8, 3);
			for (int i = 0, j = 0; i < 9; i++) {
				if (i != 8 - c) {
					A.set(j, 0, Pmu[0][i]);
					A.set(j, 1, Pmu[1][i]);
					A.set(j, 2, 1);
					B.set(j, 0, pts[i].position.x);
					B.set(j, 1, pts[i].position.y);
					B.set(j, 2, 1);
					j++;
				}
			}

			final Matrix Tc = A.solve(B).transpose();

			final Matrix P1 = Tc.times(A.transpose());
			final Matrix D = P1.minus(B.transpose());

			float e = 0;
			for (int cc = 0; cc < D.getColumnDimension(); cc++) {
				float colsum = 0;
				for (int rr = 0; rr < D.getRowDimension(); rr++) {
					colsum += D.get(rr, cc) * D.get(rr, cc);
					;
				}
				e += Math.sqrt(colsum);
			}

			if (e < emin) {
				emin = e;
				T = Tc;
			}
		}

		return T;
	}

	private static FImage loadDefaultMask() {
		try {
			return ImageUtilities.readF(FaceAligner.class.getResourceAsStream("affineMask.png"));
		} catch (final IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public FImage getMask() {
		return mask;
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		facePatchWidth = in.readInt();
		facePatchHeight = in.readInt();
		facePatchBorderPercentage = in.readFloat();
		mask = ImageUtilities.readF(in);
	}

	@Override
	public byte[] binaryHeader() {
		return this.getClass().getName().getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeInt(facePatchWidth);
		out.writeInt(facePatchHeight);
		out.writeFloat(facePatchBorderPercentage);
		ImageUtilities.write(mask, "png", out);
	}
}
