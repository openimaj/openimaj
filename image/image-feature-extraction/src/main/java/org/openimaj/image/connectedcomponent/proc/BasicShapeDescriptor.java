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
package org.openimaj.image.connectedcomponent.proc;

import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureVectorProvider;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.processor.connectedcomponent.ConnectedComponentProcessor;

/**
 * Basic descriptors of the shape of a connected component.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class BasicShapeDescriptor implements ConnectedComponentProcessor, FeatureVectorProvider<DoubleFV> {
	/**
	 * An enum of all the different basic shape descriptors.
	 */
	public enum BasicShapeDescriptorType {
		/**
		 * The area of the component
		 * 
		 * @see ConnectedComponent#calculateArea()
		 */
		AREA {
			@Override
			public DoubleFV getFeatureVector(BasicShapeDescriptor desc) {
				return new DoubleFV(new double[] { desc.area });
			}
		},
		/**
		 * The centroid of the component
		 * 
		 * @see ConnectedComponent#calculateCentroid()
		 */
		CENTROID {
			@Override
			public DoubleFV getFeatureVector(BasicShapeDescriptor desc) {
				return new DoubleFV(new double[] { desc.cx, desc.cy });
			}
		},
		/**
		 * The primary orientation of the component
		 * 
		 * @see ConnectedComponent#calculateDirection()
		 */
		DIRECTION {
			@Override
			public DoubleFV getFeatureVector(BasicShapeDescriptor desc) {
				return new DoubleFV(new double[] { desc.direction });
			}
		},
		/**
		 * The elongatedness of the component. Elongatedness is defined as the
		 * ratio of the height to width of the oriented bounding box of the
		 * component.
		 * 
		 * @see ConnectedComponent#calculateOrientatedBoundingBoxAspectRatio()
		 */
		ELONGATEDNESS {
			@Override
			public DoubleFV getFeatureVector(BasicShapeDescriptor desc) {
				return new DoubleFV(new double[] { desc.elongatedness });
			}
		},
		/**
		 * The compactness of the component. Compactness is defined as the ratio
		 * of the squared edge length of the component to its area.
		 */
		COMPACTNESS {
			@Override
			public DoubleFV getFeatureVector(BasicShapeDescriptor desc) {
				return new DoubleFV(new double[] { desc.compactness });
			}
		},
		/**
		 * The ratio of the area of the component to the area of its convex hull
		 * 
		 * @see ConnectedComponent#calculatePercentageConvexHullFit()
		 */
		CHFIT {
			@Override
			public DoubleFV getFeatureVector(BasicShapeDescriptor desc) {
				return new DoubleFV(new double[] { desc.chfit });
			}
		},
		/**
		 * The estimated number of corners of the component
		 * 
		 * @see ConnectedComponent#estimateNumberOfVertices(int, int)
		 */
		CORNERS {
			@Override
			public DoubleFV getFeatureVector(BasicShapeDescriptor desc) {
				return new DoubleFV(new double[] { desc.cornerEst });
			}
		};

		/**
		 * Create a @link{FeatureVector} representation of the specified
		 * description
		 * 
		 * @param desc
		 *            the descriptor
		 * @return the feature vector representation
		 */
		public abstract DoubleFV getFeatureVector(BasicShapeDescriptor desc);
	}

	/**
	 * The area of the component
	 * 
	 * @see ConnectedComponent#calculateArea()
	 */
	public double area;

	/**
	 * The x coordinate of the component centroid
	 * 
	 * @see ConnectedComponent#calculateCentroid()
	 */
	public double cx; // centroid x

	/**
	 * The y coordinate of the component centroid
	 * 
	 * @see ConnectedComponent#calculateCentroid()
	 */
	public double cy; // y

	/**
	 * The primary orientation of the component
	 * 
	 * @see ConnectedComponent#calculateDirection()
	 */
	public double direction;

	/**
	 * The elongatedness of the component. Elongatedness is defined as the ratio
	 * of the height to width of the oriented bounding box of the component.
	 * 
	 * @see ConnectedComponent#calculateOrientatedBoundingBoxAspectRatio()
	 */
	public double elongatedness;

	/**
	 * The compactness of the component. Compactness is defined as the ratio of
	 * the squared edge length of the component to its area.
	 */
	public double compactness;

	/**
	 * The ratio of the area of the component to the area of its convex hull
	 * 
	 * @see ConnectedComponent#calculatePercentageConvexHullFit()
	 */
	public double chfit;

	/**
	 * The estimated number of corners of the component
	 * 
	 * @see ConnectedComponent#estimateNumberOfVertices(int, int)
	 */
	public double cornerEst;

	@Override
	public void process(ConnectedComponent cc) {
		area = cc.calculateArea();

		final double[] c = cc.calculateCentroid();
		cx = c[0];
		cy = c[1];

		direction = cc.calculateDirection();

		elongatedness = cc.calculateOrientatedBoundingBoxAspectRatio();

		final float edge_length = cc.getOuterBoundary().size();
		compactness = (edge_length * edge_length) / new ConnectedComponent(cc.toPolygon()).calculateArea();

		if (area > 4)
			chfit = new ConnectedComponent(cc.toPolygon()).calculatePercentageConvexHullFit(); // chfit
																								// won't
																								// work
																								// for
																								// really
																								// small
																								// regions
		else
			chfit = 1;

		if (area > 100)
			cornerEst = cc.estimateNumberOfVertices(3, 10);
		else
			cornerEst = area;
	}

	/**
	 * Get all the values of the descriptor as an array in the order area,
	 * centroid_x, centroid_y, direction, elongatedness, compactness
	 * convex_hull_fit, corner_count
	 * 
	 * @return an array of descriptor values
	 */
	public double[] getFeatureVectorArray() {
		return new double[] { area, cx, cy, direction, elongatedness, compactness, chfit, cornerEst };
	}

	@Override
	public DoubleFV getFeatureVector() {
		return new DoubleFV(getFeatureVectorArray());
	}
}
