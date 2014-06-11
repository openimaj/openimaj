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

import org.openimaj.image.FImage;
import org.openimaj.image.processing.transform.FProjectionProcessor;
import org.openimaj.math.geometry.shape.Ellipse;
import org.openimaj.math.geometry.shape.Shape;
import org.openimaj.math.geometry.transforms.TransformUtilities;

import Jama.Matrix;

/**
 * An extension of a {@link DetectedFace} that represents the detection by an
 * ellipse. The patch is extracted from the oriented bounding box surrounding
 * the detection ellipse. The {@link Ellipse#getRotation()} is assumed to point
 * towards the bottom of the face; this means a rotation of +pi/2 equates to an
 * upright detection.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class EllipticalDetectedFace extends DetectedFace {
	Ellipse ellipse;

	/**
	 * Construct the {@link EllipticalDetectedFace} from the given parameters.
	 * 
	 * @param ellipse
	 *            the ellipse describing the location of the face in the image
	 * @param image
	 *            the image in which the face was detected
	 * @param confidence
	 *            the confidence of the detection
	 */
	public EllipticalDetectedFace(Ellipse ellipse, FImage image, float confidence) {
		super();

		this.ellipse = ellipse;
		this.bounds = ellipse.calculateRegularBoundingBox();
		this.confidence = confidence;

		if (image != null)
			this.facePatch = extractPatch(image, ellipse);
	}

	private FImage extractPatch(final FImage image, final Ellipse ellipse) {
		final float x = ellipse.calculateCentroid().getX();
		final float y = ellipse.calculateCentroid().getY();
		final double major = ellipse.getMajor();
		final double minor = ellipse.getMinor();

		final Matrix rot = TransformUtilities.rotationMatrixAboutPoint(
				-ellipse.getRotation() + Math.PI / 2,
				x,
				y);

		final Matrix translate = TransformUtilities.translateMatrix(-(x - minor), -(y - major));

		final Matrix tf = translate.times(rot);

		final FProjectionProcessor pp = new FProjectionProcessor();
		pp.setMatrix(tf);
		pp.accumulate(image);
		return pp.performProjection(0, (int) (2 * minor), 0, (int) (2 * major));
	}

	@Override
	public Shape getShape() {
		return ellipse;
	}
}
