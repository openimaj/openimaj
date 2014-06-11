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
package org.openimaj.image.processing.face.detection.keypoints;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.image.FImage;
import org.openimaj.image.analysis.pyramid.SimplePyramid;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.FaceDetector;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.image.processing.transform.ProjectionProcessor;
import org.openimaj.io.IOUtils;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.util.hash.HashCodeUtil;

import Jama.Matrix;
import Jama.SingularValueDecomposition;

/**
 * F(rontal)K(eypoint)E(nriched)FaceDetector uses an underlying face detector to
 * detect frontal faces in an image, and then looks for facial keypoints within
 * the detections.
 * <p>
 * Implementation and data is based on Mark Everingham's <a
 * href="http://www.robots.ox.ac.uk/~vgg/research/nface/">Oxford VGG Baseline
 * Face Processing Code</a>
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Reference(
		type = ReferenceType.Inproceedings,
		author = { "Mark Everingham", "Josef Sivic", "Andrew Zisserman" },
		title = "Hello! My name is... Buffy - Automatic naming of characters in TV video",
		year = "2006",
		booktitle = "In BMVC")
public class FKEFaceDetector implements FaceDetector<KEDetectedFace, FImage> {
	protected FaceDetector<? extends DetectedFace, FImage> faceDetector;
	protected FacialKeypointExtractor facialKeypointExtractor = new FacialKeypointExtractor();
	private float patchScale = 1.0f;

	/**
	 * Default constructor. Uses the standard {@link HaarCascadeDetector} with a
	 * minimum search size of 80 pixels.
	 */
	public FKEFaceDetector() {
		this(new HaarCascadeDetector(80));
	}

	/**
	 * Construct with a standard {@link HaarCascadeDetector} and the given
	 * minimum search size.
	 * 
	 * @param size
	 *            minimum detection size.
	 */
	public FKEFaceDetector(int size) {
		this(new HaarCascadeDetector(size));
	}

	/**
	 * Default constructor. Uses the standard {@link HaarCascadeDetector} with a
	 * minimum search size of 80 pixels, and the given scale-factor for
	 * extracting the face patch.
	 * 
	 * @param patchScale
	 *            the scale of the patch compared to the patch extracted by the
	 *            internal detector.
	 */
	public FKEFaceDetector(float patchScale) {
		this(new HaarCascadeDetector(80), patchScale);
	}

	/**
	 * Construct with a standard {@link HaarCascadeDetector} and the given
	 * minimum search size, and the given scale-factor for extracting the face
	 * patch.
	 * 
	 * @param patchScale
	 *            the scale of the patch compared to the patch extracted by the
	 *            internal detector.
	 * @param size
	 *            minimum detection size.
	 */
	public FKEFaceDetector(int size, float patchScale) {
		this(new HaarCascadeDetector(size), patchScale);
	}

	/**
	 * Construct with the given underlying (frontal) face detector.
	 * 
	 * @param detector
	 *            the face detector.
	 */
	public FKEFaceDetector(FaceDetector<? extends DetectedFace, FImage> detector) {
		this.faceDetector = detector;
	}

	/**
	 * Construct with the given underlying (frontal) face detector, and the
	 * given scale-factor for extracting the face patch.
	 * 
	 * @param patchScale
	 *            the scale of the patch compared to the patch extracted by the
	 *            internal detector.
	 * 
	 * @param detector
	 *            the face detector.
	 */
	public FKEFaceDetector(FaceDetector<? extends DetectedFace, FImage> detector, float patchScale) {
		this.faceDetector = detector;
		this.patchScale = patchScale;
	}

	/**
	 * Resize the image using a pyramid.
	 * 
	 * @param image
	 *            the image
	 * @param transform
	 *            the resize transform
	 * @return the resized image
	 */
	public static FImage pyramidResize(FImage image, Matrix transform) {
		// estimate the scale change
		final SingularValueDecomposition svd = transform.getMatrix(0, 1, 0, 1).svd();
		final double sv[] = svd.getSingularValues();
		final double scale = ((sv[0] + sv[1]) / 2);

		// calculate the pyramid level
		final int lev = (int) (Math.max(Math.floor(Math.log(scale) / Math.log(1.5)), 0) + 1);
		final double pyramidScale = Math.pow(1.5, (lev - 1));

		// setup the new transformed transform matrix
		final Matrix scaleMatrix = TransformUtilities.scaleMatrix(1 / pyramidScale, 1 / pyramidScale);
		final Matrix newTransform = scaleMatrix.times(transform);
		transform.setMatrix(0, 2, 0, 2, newTransform);

		return image.process(new SimplePyramid<FImage>(1.5f, lev));
	}

	/**
	 * Extract a patch from the image based on the parameters.
	 * 
	 * @param image
	 *            the image
	 * @param transform
	 *            the transform
	 * @param size
	 *            the patch size
	 * @param border
	 *            the size of the border
	 * @return the patch
	 */
	public static FImage extractPatch(FImage image, Matrix transform, int size, int border) {
		final ProjectionProcessor<Float, FImage> pp = new ProjectionProcessor<Float, FImage>();

		pp.setMatrix(transform.inverse());
		image.accumulateWith(pp);

		return pp.performProjection(border, size - border, border, size - border, RGBColour.BLACK[0]);
	}

	@Override
	public List<KEDetectedFace> detectFaces(FImage image) {
		final List<? extends DetectedFace> faces = faceDetector.detectFaces(image);

		final List<KEDetectedFace> descriptors = new ArrayList<KEDetectedFace>(faces.size());
		for (final DetectedFace df : faces) {
			final int canonicalSize = facialKeypointExtractor.getCanonicalImageDimension();
			final Rectangle r = df.getBounds();

			// calculate a scaled version of the image and extract a patch of
			// canonicalSize
			final float scale = (r.width / 2) / ((canonicalSize / 2) - facialKeypointExtractor.model.border);
			float tx = (r.x + (r.width / 2)) - scale * canonicalSize / 2;
			float ty = (r.y + (r.height / 2)) - scale * canonicalSize / 2;

			final Matrix T0 = new Matrix(new double[][] { { scale, 0, tx }, { 0, scale, ty }, { 0, 0, 1 } });
			final Matrix T = (Matrix) T0.clone();

			final FImage subsampled = pyramidResize(image, T);
			final FImage smallpatch = extractPatch(subsampled, T, canonicalSize, 0);

			// extract the keypoints
			final FacialKeypoint[] kpts = facialKeypointExtractor.extractFacialKeypoints(smallpatch);

			// calculate the transform to take the canonical coordinates to the
			// roi coordinates
			tx = (r.width / 2) - scale * canonicalSize / 2;
			ty = (r.height / 2) - scale * canonicalSize / 2;
			final Matrix T1 = new Matrix(new double[][] { { scale, 0, tx }, { 0, scale, ty }, { 0, 0, 1 } });
			FacialKeypoint.updateImagePosition(kpts, T1);

			// recompute the bounding box based on the positions of the facial
			// keypoints
			final FacialKeypoint eyeLL = FacialKeypoint.getKeypoint(kpts,
					FacialKeypoint.FacialKeypointType.EYE_LEFT_LEFT);
			final FacialKeypoint eyeRR = FacialKeypoint.getKeypoint(kpts,
					FacialKeypoint.FacialKeypointType.EYE_RIGHT_RIGHT);
			final FacialKeypoint eyeLR = FacialKeypoint.getKeypoint(kpts,
					FacialKeypoint.FacialKeypointType.EYE_LEFT_RIGHT);
			final FacialKeypoint eyeRL = FacialKeypoint.getKeypoint(kpts,
					FacialKeypoint.FacialKeypointType.EYE_RIGHT_LEFT);

			final float eyeSpace = (0.5f * (eyeRR.position.x + eyeRL.position.x))
					- (0.5f * (eyeLR.position.x + eyeLL.position.x));
			final float deltaX = (0.5f * (eyeLR.position.x + eyeLL.position.x)) - eyeSpace;
			r.x = r.x + deltaX;
			r.width = eyeSpace * 3;

			final float eyeVavg = 0.5f * ((0.5f * (eyeRR.position.y + eyeRL.position.y)) + (0.5f * (eyeLR.position.y + eyeLL.position.y)));

			r.height = 1.28f * r.width;
			final float deltaY = eyeVavg - 0.4f * r.height;
			r.y = r.y + deltaY;

			float dx = r.x;
			float dy = r.y;
			r.scaleCentroid(patchScale);
			dx = dx - r.x;
			dy = dy - r.y;
			FacialKeypoint.updateImagePosition(kpts, TransformUtilities.translateMatrix(-deltaX + dx, -deltaY + dy));

			// final KEDetectedFace kedf = new KEDetectedFace(r,
			// df.getFacePatch(), kpts, df.getConfidence());
			// final Rectangle scr = r.clone();
			final KEDetectedFace kedf = new KEDetectedFace(r, image.extractROI(r), kpts, df.getConfidence());
			descriptors.add(kedf);
		}

		return descriptors;
	}

	@Override
	public int hashCode() {
		final int hashCode = HashCodeUtil.SEED;
		HashCodeUtil.hash(hashCode, this.faceDetector);
		HashCodeUtil.hash(hashCode, this.facialKeypointExtractor);
		HashCodeUtil.hash(hashCode, this.patchScale);
		return hashCode;
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		faceDetector = IOUtils.newInstance(in.readUTF());
		faceDetector.readBinary(in);
		// facialKeypointExtractor;
		this.patchScale = in.readFloat();
	}

	@Override
	public byte[] binaryHeader() {
		return "FKED".getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeUTF(faceDetector.getClass().getName());
		faceDetector.writeBinary(out);
		// facialKeypointExtractor;
		out.writeFloat(patchScale);
	}

	@Override
	public String toString() {
		return String.format("FKEFaceDetector[innerDetector=%s]", faceDetector);
	}
}
