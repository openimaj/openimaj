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
package org.openimaj.image.objectdetection;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.image.processing.transform.ProjectionProcessor;
import org.openimaj.image.processor.SinglebandImageProcessor;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.shape.Shape;
import org.openimaj.math.geometry.transforms.TransformUtilities;

import Jama.Matrix;

/**
 * An {@link ObjectDetector} that wraps another {@link ObjectDetector} and
 * performs rotation simulations on the images it passes to the internal
 * detector. This allows a non rotation invariant detector to be able to detect
 * rotated objects.
 * <p>
 * This implementation allows the input image to be scaled in order to reduce
 * computational complexity, and control over the simulated angles.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <IMAGE>
 *            Type of image in which the detection happens
 * @param <PIXEL>
 *            Type of pixel of the image in which the detection happens
 * @param <DETECTED_OBJECT>
 *            The type of object emitted by the inner detector
 */
public class RotationSimulationObjectDetector<IMAGE extends Image<PIXEL, IMAGE> & SinglebandImageProcessor.Processable<Float, FImage, IMAGE>, PIXEL, DETECTED_OBJECT>
		implements
		ObjectDetector<IMAGE, TransformedDetection<DETECTED_OBJECT>>
{
	private ObjectDetector<IMAGE, DETECTED_OBJECT> detector;
	private Rectangle roi;
	private float scalefactor = 1f;
	private float[] simulationAngles;

	/**
	 * Construct with the given inner detector and number of rotations.
	 * Simulations will occur every 360/numRotations degrees.
	 * 
	 * @param detector
	 *            the internal detector
	 * @param numRotations
	 *            the number of rotations
	 */
	public RotationSimulationObjectDetector(ObjectDetector<IMAGE, DETECTED_OBJECT> detector, int numRotations) {
		this.detector = detector;
		this.simulationAngles = computeAngles(numRotations);
	}

	/**
	 * Construct with the given inner detector, number of rotations and scale
	 * factor. Simulations will occur every 360/numRotations degrees.
	 * 
	 * @param detector
	 *            the internal detector
	 * @param numRotations
	 *            the number of rotations
	 * @param scalefactor
	 *            the amount by which to scale the input image prior to passing
	 *            to the inner detector
	 */
	public RotationSimulationObjectDetector(ObjectDetector<IMAGE, DETECTED_OBJECT> detector, int numRotations,
			float scalefactor)
	{
		this(detector, numRotations);
		this.scalefactor = scalefactor;
	}

	/**
	 * Construct with the given inner detector, simulation angles and scale
	 * factor. Simulations will occur every 360/numRotations degrees.
	 * 
	 * @param detector
	 *            the internal detector
	 * @param simulationAngles
	 *            the rotation angles to simulate
	 * @param scalefactor
	 *            the amount by which to scale the input image prior to passing
	 *            to the inner detector
	 */
	public RotationSimulationObjectDetector(ObjectDetector<IMAGE, DETECTED_OBJECT> detector, float[] simulationAngles,
			float scalefactor)
	{
		this.detector = detector;
		this.simulationAngles = simulationAngles;
		this.scalefactor = scalefactor;
	}

	private float[] computeAngles(int numRotations) {
		final float[] angles = new float[numRotations];

		for (int i = 1; i < numRotations; i++) {
			angles[i] = (float) (2 * i * Math.PI / numRotations);
		}

		return angles;
	}

	@Override
	public List<TransformedDetection<DETECTED_OBJECT>> detect(IMAGE image) {
		final List<TransformedDetection<DETECTED_OBJECT>> results = new ArrayList<TransformedDetection<DETECTED_OBJECT>>();

		Matrix scale;

		if (scalefactor != 1) {
			image = image.process(new ResizeProcessor(scalefactor));
			scale = TransformUtilities.scaleMatrix(scalefactor, scalefactor);
		} else {
			scale = Matrix.identity(3, 3);
		}

		for (final float angle : simulationAngles) {
			if (angle == 0) {
				detectObjects(image, scale, results);
			} else {
				final Matrix matrix = TransformUtilities.rotationMatrix(angle);
				final IMAGE rimg = ProjectionProcessor.project(image, matrix);

				final Rectangle actualBounds = image.getBounds();
				final Shape transformedActualBounds = actualBounds.transform(matrix);
				final double tminX = transformedActualBounds.minX();
				final double tminY = transformedActualBounds.minY();

				final int minc = (int) Math.floor(tminX);
				final int minr = (int) Math.floor(tminY);

				matrix.set(0, 2, -minc);
				matrix.set(1, 2, -minr);

				detectObjects(rimg, matrix.times(scale), results);
			}
		}

		return results;
	}

	private void detectObjects(IMAGE image, Matrix transform, List<TransformedDetection<DETECTED_OBJECT>> results) {
		if (this.roi != null) {
			final Rectangle troi = roi.transform(transform).calculateRegularBoundingBox();
			detector.setROI(troi);
		}

		final List<DETECTED_OBJECT> detections = detector.detect(image);

		if (detections == null)
			return;

		for (final DETECTED_OBJECT o : detections) {
			results.add(new TransformedDetection<DETECTED_OBJECT>(o, transform.inverse()));
		}
	}

	@Override
	public void setROI(Rectangle roi) {
		this.roi = roi;
	}

	/**
	 * Get the internal detector
	 * 
	 * @return the internal detector
	 */
	public ObjectDetector<IMAGE, DETECTED_OBJECT> getInnerDetector() {
		return detector;
	}
}
