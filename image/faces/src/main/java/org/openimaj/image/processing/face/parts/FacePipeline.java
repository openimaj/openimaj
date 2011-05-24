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

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.openimaj.feature.local.keypoints.face.FacialDescriptor;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.image.processing.pyramid.SimplePyramid;
import org.openimaj.image.processing.transform.ProjectionProcessor;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.transforms.TransformUtilities;

import Jama.Matrix;
import Jama.SingularValueDecomposition;

public class FacePipeline {
	protected HaarCascadeDetector faceDetector;
	protected FacialKeypointExtractor facialKeypointExtractor;
	protected FacialDescriptorExtractor facialDescriptorExtractor;

	public FacePipeline() {
		try{
			faceDetector = new HaarCascadeDetector("haarcascade_frontalface_alt.xml");
			faceDetector.setMinSize(80);
		} catch(Exception e) {
			throw new RuntimeException("Could not read haarcascade file");
		}
		
		facialKeypointExtractor = new FacialKeypointExtractor();
		facialDescriptorExtractor = new FacialDescriptorExtractor();
	}
	
	protected static FImage pyramidResize(FImage image, Matrix transform) {
		//estimate the scale change
		SingularValueDecomposition svd = transform.getMatrix(0, 1, 0, 1).svd();
		double sv[] = svd.getSingularValues();
		float scale = (float) ((sv[0]+sv[1]) / 2);

		//calculate the pyramid level
		int lev = (int) (Math.max(Math.floor(Math.log(scale) / Math.log(1.5)), 0) + 1);
		double ps = Math.pow(1.5, (lev-1));
		
		//setup the new transformed transform matrix
		Matrix scaleMatrix = TransformUtilities.scaleMatrix(1/ps, 1/ps);
		Matrix newTransform = scaleMatrix.times(transform);
		transform.setMatrix(0, 2, 0, 2, newTransform);
		
		return image.process(new SimplePyramid<FImage>(1.5f, lev));
	}
	
	protected static FImage extractPatch(FImage image, Matrix transform, int size, int border) {
		ProjectionProcessor<Float, FImage> pp = new ProjectionProcessor<Float, FImage>();
		
		pp.setMatrix(transform.inverse());
		image.process(pp);
		
		return pp.performProjection(border, size-border, border, size-border, RGBColour.BLACK[0]);
	}
	
	public LocalFeatureList<FacialDescriptor> extractFaces(FImage image) {
		List<Rectangle> faces = faceDetector.detectObjects(image);
		
		MemoryLocalFeatureList<FacialDescriptor> descriptors = new MemoryLocalFeatureList<FacialDescriptor>();
		for (Rectangle r : faces) {
			int canonicalSize = facialKeypointExtractor.getCanonicalImageDimension();
			
			float scale = (r.width / 2) / ((canonicalSize / 2) - facialKeypointExtractor.model.border);
			float tx = (r.x + (r.width / 2)) - scale * canonicalSize / 2;
			float ty = (r.y + (r.height / 2)) - scale * canonicalSize / 2;
			
			Matrix T0 = new Matrix(new double[][]{ {scale, 0, tx}, {0, scale, ty}, {0, 0, 1} });
			Matrix T = (Matrix) T0.clone();
			
			FImage subsampled = pyramidResize(image, T);
			FImage patch = extractPatch(subsampled, T, canonicalSize, 0);
						
			FacialKeypoint[] kpts = facialKeypointExtractor.extractFacialKeypoints(patch);
			FacialKeypoint.updateImagePosition(kpts, T0);
			
			FacialDescriptor descr = facialDescriptorExtractor.extdesc(image, kpts);
			
			descriptors.add(descr);
		}
		
		return descriptors;
	}
	
	public static void main(String [] args) throws Exception {
		FImage image1 = ImageUtilities.readF(new File("/Volumes/Raid/face_databases/faces/image_0001.jpg"));
		List<FacialDescriptor> faces = new FacePipeline().extractFaces(image1);
		
		DisplayUtilities.display(faces.get(0).facePatch);
		System.out.println(Arrays.toString(faces.get(0).featureVector));
	}
}
