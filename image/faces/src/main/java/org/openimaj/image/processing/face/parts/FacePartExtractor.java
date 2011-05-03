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

import java.io.IOException;
import java.util.List;

import org.openimaj.feature.local.keypoints.face.FacialKeypoint;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processing.transform.ProjectionProcessor;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.image.processing.pyramid.SimplePyramid;
import org.openimaj.math.geometry.shape.Rectangle;

import Jama.Matrix;

public class FacePartExtractor {
	public LocalFeatureList<FacialKeypoint> findKeypoints(FImage image) throws IOException{
		HaarCascadeDetector detector = null;
		try{
			detector = new HaarCascadeDetector("haarcascade_frontalface_default.xml");
		}catch(Exception e){
			throw new IOException("Could not read haarcascade file");
		}
		List<Rectangle> faces = detector.detectObjects(image);
		
		FacialKeypointExtractor kptExt = new FacialKeypointExtractor();
		
		MemoryLocalFeatureList<FacialKeypoint> keypoints = new MemoryLocalFeatureList<FacialKeypoint>();
		for (Rectangle r : faces) {
			float [] BB = {r.x, r.x + r.width, r.y, r.y + r.height}; 
			float [] det = {(BB[0]+BB[1])/2, (BB[2]+BB[3])/2, (BB[1]-BB[0])/2, 1};
	        
			float scale = det[2]/(kptExt.model.imgsize/2-kptExt.model.border);
			float tx = det[0]-scale*kptExt.model.imgsize/2;
			float ty = det[1]-scale*kptExt.model.imgsize/2;
			
			Matrix T0 = new Matrix(new double[][]{{scale,0,tx},{0,scale,ty} });
			int lev= (int) (Math.max(Math.floor(Math.log(scale)/Math.log(1.5)),0)+1);
			double ps = Math.pow(1.5,(lev-1));
			scale=(float) (scale/ps);
			tx=(float) ((tx-1)/ps)+1;
			ty=(float) ((ty-1)/ps)+1;
			Matrix T = new Matrix(new double[][]{{scale,0,tx},{0,scale,ty},{ 0,0,1 } });
			
			FImage blurred = image.process(new SimplePyramid<FImage>(1.5f,lev));
//			DisplayUtilities.display(blurred);
			ProjectionProcessor<Float, FImage> pp = new ProjectionProcessor<Float, FImage>();
			pp.setMatrix(T);
			blurred.process(pp);
			FImage patch = pp.performBackProjection(0, kptExt.model.imgsize, 0, kptExt.model.imgsize, RGBColour.BLACK[0]);
			DisplayUtilities.display(patch);
			//TODO blur to prevent aliasing
//			FImage patch = image.extractROI((int)(tx), (int)(ty), (int)(kptExt.model.imgsize*scale), (int)(kptExt.model.imgsize*scale));
//			DisplayUtilities.display(patch.process(new ResizeProcessor(480,0,true)));
//			patch = patch.process(new ResizeProcessor(kptExt.model.imgsize, kptExt.model.imgsize));
//			
			Pixel[] kpts = kptExt.extractFeatures(patch);
			for (Pixel pt : kpts) {
				//rescale to image coords
				pt.x = (int) (pt.x*T0.get(0, 0) + pt.y*T0.get(0, 1) + T0.get(0, 2));
				pt.y = (int) (pt.x*T0.get(1, 0) + pt.y*T0.get(1, 1) + T0.get(1, 2));
			}
			FacialKeypoint kp = FacialDescriptor.extdesc(image, kpts);
			keypoints.add(kp);
		}
		return keypoints;
	}
}
