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
package org.openimaj.image.feature.local.interest;

import java.io.IOException;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.math.geometry.shape.Ellipse;

import Jama.Matrix;

public class HarrisIPD extends AbstractIPD {
	protected float eigenRatio = 0.04f;
	
	public HarrisIPD(float detectionScale, float integrationScale) {
		this(detectionScale, integrationScale, 0.04f);
	}
	
	public HarrisIPD(float detectionScale, float integrationScale, float eigenRatio) {
		super(detectionScale, integrationScale);
		this.eigenRatio = eigenRatio;
	}
	
	public HarrisIPD() {
		this((0.6f*2.5f)*(0.6f*2.5f), 2.5f*2.5f, 0.01f);
	}

	@Override
	public FImage createInterestPointMap() {
		FImage det = lxmxblur.multiply(lymyblur).subtractInline(lxmyblur.multiply(lxmyblur));
		FImage trace = lxmxblur.add(lymyblur);
		FImage traceSq = trace.multiply(trace);
		
		return det.subtract(traceSq.multiplyInline(eigenRatio)); 
	}

	public static void main(String [] args) throws IOException {
		//FImage image = ImageUtilities.readF(HessianIPD.class.getResource("/uk/ac/soton/ecs/jsh2/image/proc/tracking/klt/examples/cat.jpg"));
		//FImage image = ImageUtilities.readF(new File("/Users/jsh2/Downloads/affintpoints/images/car1-066-153.png"));
//		FImage image = ImageUtilities.readF(new File("/Users/jsh2/Downloads/affine_harris/pig.jpg"));
		FImage image = ImageUtilities.readF(HessianIPD.class.getResource("/org/openimaj/image/data/ellipses.jpg"));
		
		float si = 2.5f;
		float s = 0.6f;
		float sd = s*si;
		
		HarrisIPD ipd = new HarrisIPD(sd*sd, si*si, 0.01f);
		ipd.findInterestPoints(image.multiply(255f));
		
		MBFImage rgbimage = new MBFImage(image.clone(), image.clone(), image.clone()); 
		
		for (InterestPointData d : ipd.getInterestPointsThresh(100)) {
			Matrix covar = d.getCovarianceMatrix().times(d.scale); //note 10x scaling...
			
//			float [] S = new float[]{ (int)(ipd.x * Math.pow(2, scale)), (int)(ipd.y * Math.pow(2, scale)), (float) covar.get(0, 0), (float) covar.get(1, 0), (float) covar.get(1, 1) };
//			rgbimage.drawPolygon(Ellipse.ellipseFromVLFeat(S), new Float[] {1f, 0f, 0f});
			double det = covar.det();
			double a = covar.get(1,1)/det;
			double b = -(covar.get(1,0) + covar.get(0, 1)) / (2f * det);
			double c = covar.get(0, 0) / det;
			
			a /= 100;
			b /= 100;
			c /= 100;
			rgbimage.drawPolygon(Ellipse.ellipseFromOxford(d.x, d.y, (float)a, (float)b, (float)c), new Float[] {1f, 0f, 0f});
			
			System.out.println(d.x + " " + d.y + " " + d.score + " " + d.scale + " " + a + " " + b + " " + b + " " + c);
		}
		
		DisplayUtilities.display(rgbimage);
		
		System.out.println(ipd.getInterestPointsThresh(100).size());
		
		
		
	}
}
