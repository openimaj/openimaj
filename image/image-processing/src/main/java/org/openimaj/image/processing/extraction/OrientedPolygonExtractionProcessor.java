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

	private final float background;
	private final Ellipse polygonEllipse;

	/**
	 * @param polygon
	 * @param background
	 */
	public OrientedPolygonExtractionProcessor(final Polygon polygon, final float background) {
		this.polygonEllipse = polygon.toEllipse();
		this.background = background;
	}

	@Override
	public void processImage(final FImage image) {
		image.internalAssign(this.orientedBoundingBoxProjection(image));
	}

	private FImage orientedBoundingBoxProjection(final FImage image) {
		final ProjectionProcessor<Float, FImage> pp = new ProjectionProcessor<Float,FImage>();
		Matrix trans = Matrix.identity(3, 3);
		trans = trans.times(TransformUtilities.rotationMatrix(-this.polygonEllipse.getRotation()));
		trans = trans.times(
			TransformUtilities.translateToPointMatrix(
					this.polygonEllipse.calculateCentroid(),
					new Point2dImpl(0,0))
		);
		pp.setMatrix(trans);
		pp.accumulate(image);
		return pp.performProjection(
				(int)-this.polygonEllipse.getMajor(),(int)this.polygonEllipse.getMajor(),
				(int)-this.polygonEllipse.getMinor(),(int)this.polygonEllipse.getMinor(),
				this.background
		);
	}

}
