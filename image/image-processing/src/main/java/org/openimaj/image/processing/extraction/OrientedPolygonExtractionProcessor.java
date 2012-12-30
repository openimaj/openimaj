package org.openimaj.image.processing.extraction;

import org.openimaj.image.FImage;
import org.openimaj.image.processing.transform.ProjectionProcessor;
import org.openimaj.image.processor.ImageProcessor;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Ellipse;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.transforms.TransformUtilities;

import Jama.Matrix;

/**
 * Extract a polygon from an image into a new image.
 * The ellipse fitting the polygon is extracted.
 * The rotation of the ellipse is used to orientate the polygon.
 * The major and minor axis of the ellipse form the dimensions of the output FImage
 * The {@link ProjectionProcessor} is used to perform the extraction. Concretely, 
 * the projection processor is set up with a transform matrix combining the rotation of
 * the ellipse and a translation using the COG of the ellipse.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class OrientedPolygonExtractionProcessor implements ImageProcessor<FImage>{
	
	private float background;
	private Ellipse polygonEllipse;

	/**
	 * @param polygon
	 * @param background
	 */
	public OrientedPolygonExtractionProcessor(Polygon polygon, float background) {
		this.polygonEllipse = polygon.toEllipse();
		this.background = background;
	}
	
	@Override
	public void processImage(FImage image) {
		image.internalAssign(orientedBoundingBoxProjection(image));
	}
	
	private FImage orientedBoundingBoxProjection(FImage image) {
		ProjectionProcessor<Float, FImage> pp = new ProjectionProcessor<>();
		Matrix trans = Matrix.identity(3, 3);
		trans = trans.times(TransformUtilities.rotationMatrix(-polygonEllipse.getRotation()));
		trans = trans.times(
			TransformUtilities.translateToPointMatrix(
					polygonEllipse.getCOG(), 
					new Point2dImpl(0,0))
		);
		pp.setMatrix(trans);
		pp.accumulate(image);
		return pp.performProjection(
				(int)-polygonEllipse.getMajor(),(int)polygonEllipse.getMajor(),
				(int)-polygonEllipse.getMinor(),(int)polygonEllipse.getMinor(),
				background
		);
	}

}
