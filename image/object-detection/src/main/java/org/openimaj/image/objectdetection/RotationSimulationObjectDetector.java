package org.openimaj.image.objectdetection;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.Image;
import org.openimaj.image.processing.transform.ProjectionProcessor;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.shape.Shape;
import org.openimaj.math.geometry.transforms.TransformUtilities;

import Jama.Matrix;

/**
 * An {@link ObjectDetector} that wraps another {@link ObjectDetector} and
 * performs rotation simulations on the images it passes to the internal
 * detector. This allows a non rotation invariant detector to become detect
 * rotated objects.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <IMAGE>
 *            Type of image in which the detection happens
 * @param <PIXEL>
 *            Type of pixel of the image in which the detection happens
 * @param <DETECTED_OBJECT>
 *            The type of extracted detected object
 */
public class RotationSimulationObjectDetector<IMAGE extends Image<PIXEL, IMAGE>, PIXEL, DETECTED_OBJECT>
		implements
		ObjectDetector<IMAGE, TransformedDetection<DETECTED_OBJECT>>
{
	private ObjectDetector<IMAGE, DETECTED_OBJECT> detector;
	private int numRotations = 1;
	private Rectangle roi;

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
		this.numRotations = numRotations;
	}

	@Override
	public List<TransformedDetection<DETECTED_OBJECT>> detect(IMAGE image) {
		final List<TransformedDetection<DETECTED_OBJECT>> results = new ArrayList<TransformedDetection<DETECTED_OBJECT>>();

		detectObjects(image, Matrix.identity(3, 3), results);

		for (int i = 1; i < numRotations; i++) {
			final double angle = i * 2 * Math.PI / numRotations;

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

			// final Matrix matrix =
			// TransformUtilities.rotationMatrixAboutPoint(angle,
			// image.getWidth() / 2,
			// image.getHeight() / 2);
			// final IMAGE rimg = image.transform(matrix);

			// DisplayUtilities.displayName(rimg, "" + i);

			detectObjects(rimg, matrix, results);
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

	public ObjectDetector<IMAGE, DETECTED_OBJECT> getInnerDetector() {
		return detector;
	}
}
