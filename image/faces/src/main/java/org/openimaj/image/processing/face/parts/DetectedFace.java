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
package org.openimaj.image.processing.face.parts;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.processing.face.parts.FacialKeypoint.FacialKeypointType;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.shape.Rectangle;

import Jama.Matrix;

/**
 * 
 * @author Jonathon Hare
 *
 */
public class DetectedFace implements Serializable {
	public class DetectedFacePart implements Serializable {
		private static final long serialVersionUID = 1L;
		
		public FacialKeypointType type;
		public Point2d position;
		public float [] featureVector;
		
		public DetectedFacePart(FacialKeypointType type, Point2d position) {
			this.type = type;
			this.position = position;
		}
		
		public FImage getImage() {
			FImage image = new FImage(2*featureRadius+1,2*featureRadius+1);
			
			for (int i=0, rr=-featureRadius; rr<=featureRadius; rr++) {
				for (int cc=-featureRadius; cc<=featureRadius; cc++) {
					float r2 = rr*rr + cc*cc;
					
					if (r2<=featureRadius*featureRadius) { //inside circle
						float value = featureVector[i++];
						
						image.pixels[rr + featureRadius][cc + featureRadius] = value < -3 ? 0 : value >=3 ? 1 : (3f + value) / 6f;  
					}
				}
			}
			
			return image;
		}
	}
	
	static final long serialVersionUID = 1L;
		
	/**
	 * Affine projection from flat,vertically oriented face to located face space.
	 * You'll probably need to invert this if you want to use it to extract the face
	 * from the image.
	 */
	public Matrix transform;
	
	/** The size of the sampling circle for constructing individual features */
	public int featureRadius;
	
	/** A patch depicting the whole face, with a transform applied to align all the points */
	public FImage affineFacePatch;
	
	/** A patch depicting the whole face, normalised so the eyes are level and in fixed positions */
	public FImage facePatch;
	
	/** A list of all the parts of the face */
	public List<DetectedFacePart> faceParts = new ArrayList<DetectedFacePart>();
	
	public Rectangle bounds;

	public FImage warpFacePatch;
	
	public DetectedFace() {}
	
	public DetectedFacePart getPartDescriptor(FacialKeypointType type) {
		if (faceParts.get(type.ordinal()).type == type)
			return faceParts.get(type.ordinal());
		
		for (DetectedFacePart part : faceParts) 
			if (part.type == type) 
				return part;
		
		return null;
	}
}
