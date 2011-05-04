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
package org.openimaj.image.feature.local.affine;

import java.io.File;
import java.io.IOException;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.engine.DoGSIFTEngineOptions;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.io.IOUtils;


public class ASIFT extends AffineSimulation<LocalFeatureList<Keypoint>, Keypoint> {
	DoGSIFTEngine keypointEngine;
	
	public ASIFT(boolean hires) {
		super();
		
		DoGSIFTEngineOptions opts = new DoGSIFTEngineOptions();
		opts.setDoubleInitialImage(hires);		
		keypointEngine = new DoGSIFTEngine(opts);
	}
	
	public ASIFT(DoGSIFTEngineOptions opts) {
		super();
		keypointEngine = new DoGSIFTEngine(opts);
	}

	@Override
	protected LocalFeatureList<Keypoint> newList() {
		return new MemoryLocalFeatureList<Keypoint>();
	}
	
	@Override
	protected LocalFeatureList<Keypoint> findKeypoints(FImage image) {
		LocalFeatureList<Keypoint> keys = keypointEngine.findFeatures(image);

		return keys;
	}
	
//	@Override
//	void transformToOriginal(LocalFeatureList<Keypoint> keys, FImage original, float Rtheta, float t1, float t2) {
//		List<Keypoint> keys_to_remove = new ArrayList<Keypoint>();
//		float x_ori, y_ori;
//		Rtheta = Rtheta*PI/180;
//
//		if ( Rtheta <= PI/2 )
//		{
//			x_ori = 0;
//			y_ori = (float) ((original.cols) * Math.sin(Rtheta) / t1);
//		}
//		else
//		{
//			x_ori = (float) (-(original.cols) * Math.cos(Rtheta) / t2);
//			y_ori = (float) (( (original.cols) * Math.sin(Rtheta) + (original.rows) * Math.sin(Rtheta-PI/2) ) / t1);
//		}
//
//		float sin_Rtheta = (float) Math.sin(Rtheta);
//		float cos_Rtheta = (float) Math.cos(Rtheta);
//
//		for (Keypoint k : keys) {
//			//deal with angle changes
//			float x = (float) (k.col + Math.cos(k.ori));
//			float y = (float) (k.row + Math.sin(k.ori));
//			x = x - x_ori;
//			y = y - y_ori;
//			x = x*t2;
//			y = y*t1;
//			float otx = cos_Rtheta*x - sin_Rtheta*y;
//			float oty = sin_Rtheta*x + cos_Rtheta*y;
//			x = otx;
//			y = oty;
//			
//			/* project the coordinates of im1 to original image before tilt-rotation transform */
//			/* Get the coordinates with respect to the 'origin' of the original image before transform */
//			k.col = k.col - x_ori;
//			k.row = k.row - y_ori;
//			/* Invert tilt */
//			k.col = k.col * t2;
//			k.row = k.row * t1;
//			/* Invert rotation (Note that the y direction (vertical) is inverse to the usual 
//			 * concention. Hence Rtheta instead of -Rtheta to inverse the rotation.) */
//			float tx = cos_Rtheta*k.col - sin_Rtheta*k.row;
//			float ty = sin_Rtheta*k.col + cos_Rtheta*k.row;
//			
//			k.col = tx;
//			k.row = ty;
//			
//			float dx = k.col - x;
//			float dy = k.row - y;
//			k.ori = (float) Math.atan2(dy, dx);
//			
//			if(tx <= 0 || ty <= 0 || tx >= original.getWidth() || ty >= original.getHeight()) {
//				keys_to_remove.add(k);
//			} 
//		}
//		keys.removeAll(keys_to_remove);	
//	}
	
	public static void main(String [] args) throws IOException {
		File imageFile;
		boolean split = false;
		
		if (args.length==3 && args[0].equals("-split")) {
			split = true;
			imageFile = new File(args[1]);
		} else {
			imageFile = new File(args[0]);
		}
		
		FImage image = ImageUtilities.readF(imageFile);
		ASIFT imgs = new ASIFT(false);
		imgs.process(image, 5);
		
		if (split) {
			for (AffineParams params : imgs.getKeypointsMap().keySet()) {
				File out;
				if (params.theta == 0 && params.tilt == 1)
					out = new File(args[2]);
				else
					if (args[2].endsWith(".key"))
						out = new File(args[2].replace(".key", String.format("_%f_%f.key", params.theta, params.tilt)));
					else
						out = new File(args[2] += String.format("_%f_%f", params.theta, params.tilt));
					
				IOUtils.writeASCII(out, imgs.getKeypointsMap().get(params));
			}
		} else {
			IOUtils.writeASCII(System.out, imgs.getKeypoints());
		}
	}
}
