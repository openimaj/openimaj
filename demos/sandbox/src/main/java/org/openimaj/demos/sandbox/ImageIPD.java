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
package org.openimaj.demos.sandbox;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.openimaj.demos.utils.FeatureClickListener;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.feature.local.interest.HarrisIPD;
import org.openimaj.image.feature.local.interest.IPDSelectionMode;
import org.openimaj.image.feature.local.interest.InterestPointData;
import org.openimaj.image.feature.local.interest.InterestPointVisualiser;
import org.openimaj.io.IOUtils;
import org.openimaj.io.wrappers.ReadWriteableListBinary;

public class ImageIPD {
	
	static abstract class ReadWriteableIPDList<T extends InterestPointData> extends ReadWriteableListBinary<T>{
		
		public ReadWriteableIPDList() {
			super(new ArrayList<T>());
		}
		
		public ReadWriteableIPDList(List<T> list) {
			super(list);
		}

		@Override
		protected void writeValue(T v, DataOutput out)throws IOException {
			v.writeBinary(out);
			
		}

		@Override
		protected T readValue(DataInput in) throws IOException {
			T c = createFeature();
			c.readBinary(in);
			return c;
		}

		protected abstract T createFeature() ;
		
	}
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException{
		
		MBFImage image = ImageUtilities.readMBF(new File("/Users/ss/Development/descriptors/img1.png"));
		FImage fimage = Transforms.calculateIntensity(image).multiply(255f);
		
		File featureOut = new File("/Users/ss/Development/descriptors/img1-oi.features");
		List<InterestPointData> kps  = null;
		boolean force = true;
		float sd = (float) 1;
		HarrisIPD harrisIPD = new HarrisIPD(sd*0.7f,sd*1f,0.01f);
		if(!featureOut.exists() || force){
			harrisIPD.findInterestPoints(fimage);
			kps = new IPDSelectionMode.Threshold(10000).selectPoints(harrisIPD);
//			kps = new IPDSelectionMode.All().selectPoints(harrisIPD);
			IOUtils.writeBinary(featureOut,new ReadWriteableIPDList<InterestPointData>(kps){
				@Override
				protected InterestPointData createFeature() {
					return new InterestPointData();
				}
			});
		}
		else{
			kps = IOUtils.read(featureOut,ReadWriteableIPDList.class).value;
		}
		
		
		InterestPointVisualiser<Float[], MBFImage> visualiser = InterestPointVisualiser.visualiseInterestPoints(image, kps);
		MBFImage out = visualiser.drawPatches(RGBColour.RED, RGBColour.GREEN);
		
		JFrame f = DisplayUtilities.display(out,String.format("Showing %d feature", kps.size()));
		FeatureClickListener l = new FeatureClickListener();
		l.setImage(kps, image);
		f.getContentPane().addMouseListener(l);
		
	}
}
