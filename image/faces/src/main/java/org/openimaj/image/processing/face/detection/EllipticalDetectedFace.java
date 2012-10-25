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
		final float x = ellipse.getCOG().getX();
		final float y = ellipse.getCOG().getY();
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
