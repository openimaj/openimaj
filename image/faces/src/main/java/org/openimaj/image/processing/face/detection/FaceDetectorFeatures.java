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

import java.util.List;
import java.util.Set;

import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureVector;
import org.openimaj.feature.MultidimensionalIntFV;
import org.openimaj.image.Image;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.math.geometry.shape.Polygon;

/**
 * Simple features that can be extracted from a list of detected faces and an
 * image. Contains things like the count of faces, bounding boxes, etc.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public enum FaceDetectorFeatures {
	/**
	 * Count the faces in the image. Returns the count as a single element,
	 * 1-dimensional {@link MultidimensionalIntFV}.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	COUNT {
		@Override
		public <T extends Image<?, T>> FeatureVector getFeatureVector(List<? extends DetectedFace> faces, T img) {
			return new MultidimensionalIntFV(new int[] { faces.size() }, 1);
		}
	},
	/**
	 * Get the set of pixels describing each face. Returns the result as a 2d
	 * {@link MultidimensionalIntFV} where each row has interlaced x and y
	 * values: x1, y1, x2, y2, ...
	 * 
	 * The returned feature is not square; the length of each row is dependent
	 * on the number of pixels associated with the respective face.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	BLOBS {
		@Override
		public <T extends Image<?, T>> FeatureVector getFeatureVector(List<? extends DetectedFace> faces, T img) {
			final int[][] fvs = new int[faces.size()][];
			int i = 0;

			for (final DetectedFace df : faces) {
				final Set<Pixel> pixels = getConnectedComponent(df).pixels;

				final int[] fv = new int[pixels.size() * 2];

				int j = 0;
				for (final Pixel p : pixels) {
					fv[j++] = p.x;
					fv[j++] = p.y;
				}

				fvs[i++] = fv;
			}

			return new MultidimensionalIntFV(fvs);
		}
	},
	/**
	 * Get the bounding box describing each face. The bounding boxes are encoded
	 * as a 2D {@link MultidimensionalIntFV} with each row corresponding to a
	 * face. Each row is encoded as the x, y, width, height values of the
	 * bounding box.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	BOX {
		@Override
		public <T extends Image<?, T>> FeatureVector getFeatureVector(List<? extends DetectedFace> faces, T img) {
			final int[][] fvs = new int[faces.size()][];
			int i = 0;

			for (final DetectedFace df : faces) {
				fvs[i++] = new int[] {
						(int) df.getBounds().x,
						(int) df.getBounds().y,
						(int) df.getBounds().width,
						(int) df.getBounds().height
				};
			}

			return new MultidimensionalIntFV(fvs);
		}
	},
	/**
	 * Get the oriented bounding box describing each face. The bounding boxes
	 * are encoded as a 2D {@link MultidimensionalIntFV} with each row
	 * corresponding to a face. Each row is encoded as the the four corners of
	 * the bounding box: x1, y1, x2, y2, x3, y3, x4, y4.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	ORIBOX {
		@Override
		public <T extends Image<?, T>> FeatureVector getFeatureVector(List<? extends DetectedFace> faces, T img) {
			final int[][] fvs = new int[faces.size()][];
			int i = 0;

			for (final DetectedFace df : faces) {
				final Polygon p = getConnectedComponent(df).calculateOrientatedBoundingBox().asPolygon();

				final int[] fv = new int[p.getVertices().size() * 2];

				for (int j = 0, k = 0; j < fv.length; j += 2, k++) {
					fv[j] = (int) p.getVertices().get(k).getX();
					fv[j + 1] = (int) p.getVertices().get(k).getY();
				}

				fvs[i++] = fv;
			}

			return new MultidimensionalIntFV(fvs);
		}
	},
	/**
	 * Get the relative area of each detected face (normalised by the image
	 * area). The returned feature is a 1D {@link DoubleFV} with each element
	 * corresponding to an individual face detection.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	AREA {
		@Override
		public <T extends Image<?, T>> FeatureVector getFeatureVector(List<? extends DetectedFace> faces, T img) {
			final double[] fv = new double[faces.size()];
			final double area = img.getWidth() * img.getHeight();
			int i = 0;

			for (final DetectedFace df : faces) {
				fv[i++] = getConnectedComponent(df).calculateArea() / area;
			}

			return new DoubleFV(fv);
		}
	};

	protected ConnectedComponent getConnectedComponent(DetectedFace df) {
		if (df instanceof CCDetectedFace) {
			return ((CCDetectedFace) df).connectedComponent;
		} else {
			return new ConnectedComponent(df.getBounds());
		}
	}

	/**
	 * Compute a feature vector describing the detections.
	 * 
	 * @param <T>
	 *            Type of {@link Image}
	 * @param faces
	 *            The detected faces.
	 * @param img
	 *            The image the faces were detected from.
	 * @return a feature vector.
	 */
	public abstract <T extends Image<?, T>> FeatureVector getFeatureVector(List<? extends DetectedFace> faces, T img);
}
