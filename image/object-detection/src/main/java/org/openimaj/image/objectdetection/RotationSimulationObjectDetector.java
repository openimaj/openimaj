package org.openimaj.image.objectdetection;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.Image;
import org.openimaj.image.objectdetection.RotationSimulationObjectDetector.TransformedDetection;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.transforms.TransformUtilities;

import Jama.Matrix;

public class RotationSimulationObjectDetector<IMAGE extends Image<PIXEL, IMAGE>, PIXEL, DETECTED_OBJECT>
		implements
			ObjectDetector<IMAGE, TransformedDetection<DETECTED_OBJECT>>
{
	ObjectDetector<IMAGE, DETECTED_OBJECT> detector;
	private int numRotations = 8;
	private Rectangle roi;

	public RotationSimulationObjectDetector(ObjectDetector<IMAGE, DETECTED_OBJECT> detector) {
		this.detector = detector;
	}

	public static class TransformedDetection<DETECTED_OBJECT> {
		public Matrix transform;
		public DETECTED_OBJECT detection;

		TransformedDetection(DETECTED_OBJECT detection, Matrix transform) {
			this.detection = detection;
			this.transform = transform;
		}

	}

	@Override
	public List<TransformedDetection<DETECTED_OBJECT>> detect(IMAGE image) {
		final List<TransformedDetection<DETECTED_OBJECT>> results = new ArrayList<TransformedDetection<DETECTED_OBJECT>>();

		detectObjects(image, Matrix.identity(3, 3), results);

		for (int i = 1; i < numRotations; i++) {
			final double angle = i * 2 * Math.PI / numRotations;
			// final Matrix matrix = TransformUtilities.rotationMatrix(angle);
			// final IMAGE rimg = ProjectionProcessor.project(image, matrix);

			final Matrix matrix = TransformUtilities.rotationMatrixAboutPoint(angle, image.getWidth() / 2,
					image.getHeight() / 2);
			final IMAGE rimg = image.transform(matrix);

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
