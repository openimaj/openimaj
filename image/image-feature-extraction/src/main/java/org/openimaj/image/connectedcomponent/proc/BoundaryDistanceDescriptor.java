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

import java.util.List;

import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureVectorProvider;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.pixel.ConnectedComponent.ConnectMode;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processor.connectedcomponent.ConnectedComponentProcessor;
import org.openimaj.math.util.Interpolation;

/**
 * Distance-from-centroid descriptor for convex shapes. Sweeps the 
 * edge of the shape over all angles in 0..360 and records the distance
 * from the centroid.
 * 
 * Scale invariance is optionally achieved by normalising the
 * resultant vector to sum to 1.
 * 
 * Rotation invariance is optionally achieved by measuring angles
 * from the dominant orientation of the connected component.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class BoundaryDistanceDescriptor implements ConnectedComponentProcessor, FeatureVectorProvider<DoubleFV> {
	/**
	 * The number of samples
	 */
	public final static int DESCRIPTOR_LENGTH = 360;
	
	/**
	 * The descriptor vector, measusring distance from centroid per degree
	 */
	public double [] descriptor = new double[DESCRIPTOR_LENGTH];
	protected boolean normaliseScale;
	protected boolean normaliseAngle;

	/**
	 * Construct the BoundaryDistanceDescriptor with both scale and
	 * orientation normalisation enabled
	 */
	public BoundaryDistanceDescriptor() {
		this(true, true);
	}
	
	/**
	 * Construct the BoundaryDistanceDescriptor with optional scale and
	 * orientation invariance.
	 * @param normaliseDistance enable scale invariance
	 * @param normaliseAngle enable rotation invariance
	 */
	public BoundaryDistanceDescriptor(boolean normaliseDistance, boolean normaliseAngle) {
		this.normaliseScale = normaliseDistance;
		this.normaliseAngle = normaliseAngle;
	}

	@Override
	public void process(ConnectedComponent cc) {
		cc = new ConnectedComponent(cc.calculateConvexHull()); //make shape convex
		
		List<Pixel> bound = cc.getInnerBoundary(ConnectMode.CONNECT_8);
		double [] centroid = cc.calculateCentroid();
		double direction = cc.calculateDirection();

		float[] distances = new float[bound.size()];
		float[] angle = new float[bound.size()];
		int count = 0;

		for (int i=0; i<bound.size(); i++) {
			Pixel p = bound.get(i);
			double o = p.y - centroid[1];
			double a = p.x - centroid[0];

			float dist = (float) Math.sqrt((a*a) + (o*o));
			distances[i] = dist;

			if (normaliseAngle) {
				angle[i] = (float) (direction - Math.atan2(o, a));
			} else {
				angle[i] = (float) (Math.atan2(o, a));
			}
					
			angle[i] = (float) ((angle[i] %= 2.0*Math.PI) >= 0 ? angle[i] : (angle[i] + 2.0*Math.PI));			
			angle[i] = (float) (360.0 * angle[i] / (2.0*Math.PI));
		}

		for (int i=0; i<DESCRIPTOR_LENGTH; i++) {
			int index1 = -1;
			int index2 = -1;

			for (int j=0; j<angle.length; j++) {
				int n = ((j+1 == angle.length) ? 0 : j+1);

				float aj = angle[j];
				float an = angle[n];

				if (an > 350 && aj < 10) if (i<10) an-=360; else aj+=360;
				if (aj > 350 && an < 10) if (i<10) aj-=360; else an+=360;
				
				if (aj==i) {
					index1 = j;
					index2 = j;
					break;
				} else if (aj<an) {
					if (i <= an && i > aj) {
						index1 = j;
						index2 = n;
						break;
					}
				} else {
					if (i <= aj && i > an) {
						index1 = j;
						index2 = n;
						break;
					}
				}
			}

			
			descriptor[i] = Interpolation.lerp(i, angle[index1], distances[index1], angle[index2], distances[index2]);
			count += descriptor[i];
		}
		
		if (normaliseScale) {
			for (int i=0; i<DESCRIPTOR_LENGTH; i++)
				descriptor[i] /= count;
		}
	}

	/**
	 * Get the feature vector as a double array
	 * @return the feature vector
	 */
	public double[] getFeatureVectorArray() {
		return descriptor;
	}
	
	@Override
	public DoubleFV getFeatureVector() {
		return new DoubleFV(getFeatureVectorArray());
	}
}
