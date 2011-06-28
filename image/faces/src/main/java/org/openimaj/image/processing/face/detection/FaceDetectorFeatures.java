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

public enum FaceDetectorFeatures {
	COUNT {
		@Override
		public <T extends Image<?,T>> FeatureVector getFeatureVector(List<? extends DetectedFace> faces, T img) {
			return new MultidimensionalIntFV(new int[] { faces.size() }, 1);
		}
	},
	BLOBS {
		@Override
		public <T extends Image<?,T>> FeatureVector getFeatureVector(List<? extends DetectedFace> faces, T img) {
			int [][] fvs = new int[faces.size()][];
			int i=0;
			
			for (DetectedFace df : faces) {
				Set<Pixel> pixels = getConnectedComponent(df).pixels;
				
				int [] fv = new int[pixels.size() * 2];
				
				int j=0;
				for (Pixel p : pixels) {
					fv[j++] = p.x;
					fv[j++] = p.y;
				}
				
				fvs[i++] = fv;
			}
			
			return new MultidimensionalIntFV(fvs);
		}
	},
	BOX {
		@Override
		public <T extends Image<?,T>> FeatureVector getFeatureVector(List<? extends DetectedFace> faces, T img) {
			int [][] fvs = new int[faces.size()][];
			int i=0;
			
			for (DetectedFace df : faces) {
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
	ORIBOX {
		@Override
		public <T extends Image<?,T>> FeatureVector getFeatureVector(List<? extends DetectedFace> faces, T img) {
			int [][] fvs = new int[faces.size()][];
			int i=0;
			
			for (DetectedFace df : faces) {
				Polygon p = getConnectedComponent(df).calculateOrientatedBoundingBox();
				
				int [] fv = new int[p.getVertices().size() * 2];
				
				for (int j=0, k=0; j<fv.length; j+=2, k++) {
					fv[j] = (int) p.getVertices().get(k).getX();
					fv[j+1] = (int) p.getVertices().get(k).getY();
				}
				
				fvs[i++] = fv;
			}
			
			return new MultidimensionalIntFV(fvs);
		}
	}, 
	AREA {
		@Override
		public <T extends Image<?,T>> FeatureVector getFeatureVector(List<? extends DetectedFace> faces, T img) {
			double [] fv = new double[faces.size()];
			double area = img.getWidth() * img.getHeight();
			int i=0;
			
			for (DetectedFace df : faces) {
				fv[i++] = getConnectedComponent(df).calculateArea() / area;
			}
			
			return new DoubleFV(fv);
		}
	}
	;

	protected ConnectedComponent getConnectedComponent(DetectedFace df) {
		if (df instanceof CCDetectedFace) {
			return ((CCDetectedFace)df).connectedComponent;
		} else {
			return new ConnectedComponent(df.getBounds());
		}
	}
	
	public abstract <T extends Image<?,T>> FeatureVector getFeatureVector(List<? extends DetectedFace> faces, T img);
}
