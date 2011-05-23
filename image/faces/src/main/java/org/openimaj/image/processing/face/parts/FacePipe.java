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
import java.util.ArrayList;
import java.util.List;

import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FloatFV;
import org.openimaj.feature.FloatFVComparison;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processing.algorithm.EqualisationProcessor;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.image.processing.pyramid.SimplePyramid;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.image.processing.transform.ProjectionProcessor;
import org.openimaj.math.geometry.shape.Rectangle;

import Jama.Matrix;

import cern.colt.Arrays;

public class FacePipe {
	public static void main(String [] args) throws Exception {
		FImage image1 = ImageUtilities.readF(new File("/Volumes/Raid/face_databases/faces/image_0001.jpg"));
		List<FloatFV> f1 = getFeatures(image1);
		
		FImage image2 = ImageUtilities.readF(new File("/Volumes/Raid/face_databases/faces/image_0001.jpg"));
		List<FloatFV> f2 = getFeatures(image2);
		
		System.out.println(f1.get(0).compare(f2.get(0), FloatFVComparison.EUCLIDEAN));
	}
	
	public static List<FloatFV> getFeatures(FImage image) throws Exception {
		HaarCascadeDetector detector = new HaarCascadeDetector("haarcascade_frontalface_alt.xml");
		List<Rectangle> faces = detector.detectObjects(image);
		
		FacialKeypointExtractor kptExt = new FacialKeypointExtractor();
		//faces.get(0).setBounds(240.00f,69.00f,199.00f,199.00f);
		//faces.get(0).setBounds(236f,64.00f,211f,211f);
		
//		faces.add(new Rectangle(240.00f,69.00f,199.00f,199.00f));
//		faces.add(new Rectangle(236f,64.00f,211f,211f));
		
		List<FloatFV> features = new ArrayList<FloatFV>();
		
		for (Rectangle r : faces) {
			float [] BB = {r.x, r.x + r.width, r.y, r.y + r.height}; 
			float [] det = {(BB[0]+BB[1])/2, (BB[2]+BB[3])/2, (BB[1]-BB[0])/2, 1};
	        
			float scale0 = det[2]/(kptExt.model.imgsize/2 - kptExt.model.border);
			float tx0 = det[0]-scale0*kptExt.model.imgsize/2;
			float ty0 = det[1]-scale0*kptExt.model.imgsize/2;
			
			
//			DisplayUtilities.display(image, "image");
			
			//TODO blur to prevent aliasing
			FImage patch = image.extractROI((int)(tx0), (int)(ty0), (int)(kptExt.model.imgsize*scale0), (int)(kptExt.model.imgsize*scale0));
//			DisplayUtilities.display(patch, "patch");
			patch = patch.process(new ResizeProcessor(kptExt.model.imgsize, kptExt.model.imgsize));
//			DisplayUtilities.display(patch, "patch");
			
			Pixel[] kpts = kptExt.extractFeatures(patch);
			
			FImage image2 = image.clone();
			for (Pixel pt : kpts) {
				patch.drawPoint(pt, 1f, 1);
				
				//rescale to image coords
				pt.x = (int) (pt.x*scale0 + tx0);
				pt.y = (int) (pt.y*scale0 + ty0);
				image2.drawPoint(pt, 1f, 3);
			}
			
			DisplayUtilities.display(patch);
			DisplayUtilities.display(image2);
			
			float [] desc = FacialDescriptor.extdesc(image, kpts).featureVector;
			
			features.add(new FloatFV(desc));
			
			System.out.println(desc.length);
			System.out.println(Arrays.toString(desc));
			
		}
		
		return features;
	}
}
